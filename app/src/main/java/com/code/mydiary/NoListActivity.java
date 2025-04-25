package com.code.mydiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.code.mydiary.util.GenderResourceUtil;
import com.code.mydiary.util.NoListAdapter;
import com.code.mydiary.util.ToastUtil;
import com.code.mydiary.util.UserCRUD;

public class NoListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoListAdapter adapter;
    private ArrayList<String> noList = new ArrayList<>();
    private long userId;
    private UserCRUD userCRUD;
    private EditText titleEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_list_activity);

        // 修改：返回按钮逻辑
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed()); // 让btn_back等效于系统返回键

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            ToastUtil.showMsg(NoListActivity.this, "用户ID无效，请重新登录");
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recycler_no);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userCRUD = new UserCRUD(this);
        userCRUD.open();

        loadNoList();

        adapter = new NoListAdapter(noList, this::onItemContentChanged);
        recyclerView.setAdapter(adapter);
        titleEditText = findViewById(R.id.title_no);

        // 加载标题
        String title = userCRUD.getNoListTitle(userId);
        titleEditText.setText(title);


        initSexChange();
    }

    private void onItemContentChanged(int position, String content) {
        if (position < noList.size()) {
            noList.set(position, content);
        } else {
            noList.add(content);
        }
        if (position == noList.size() - 1 && !content.trim().isEmpty()) {
            noList.add("");
            // 用post延迟刷新，避免RecyclerView布局期间崩溃
            recyclerView.post(() -> adapter.notifyItemInserted(noList.size() - 1));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNoList();
        // 保存标题
        String title = titleEditText.getText().toString().trim();
        if (title.isEmpty()) title = "禁止事项 Ver.1";
        userCRUD.saveNoListTitle(userId, title);
    }

    private void loadNoList() {
        noList.clear();
        ArrayList<String> dbList = userCRUD.getNoList(userId);
        if (dbList != null) {
            noList.addAll(dbList);
        }
        // 只在显示时补空项
        while (noList.size() < 10) {
            noList.add("");
        }
    }

    private void saveNoList() {
        // 只保存非空项，避免数据库膨胀
        ArrayList<String> saveList = new ArrayList<>();
        for (String item : noList) {
            if (!item.trim().isEmpty()) {
                saveList.add(item);
            }
        }
        userCRUD.saveNoList(userId, saveList);
    }

    //根据性别更新
    private void initSexChange() {
        // 动态设置顶部背景
        EditText titleNo = findViewById(R.id.title_no);
        if (titleNo != null) {
            int color = getResources().getColor(
                    GenderResourceUtil.getTabMainColorRes(this)
            );
            titleNo.setBackgroundColor(color);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        userCRUD.close();
    }

    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NoListActivity.this, Menu.class);
        intent.putExtra("user_id", userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}