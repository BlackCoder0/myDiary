package com.code.mydiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;

import com.code.mydiary.util.UserCRUD;

public class NoListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoListAdapter adapter;
    private ArrayList<String> noList = new ArrayList<>();
    private long userId;
    private UserCRUD userCRUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_list_activity);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "用户ID无效，请重新登录", Toast.LENGTH_SHORT).show();
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
    }

    private void onItemContentChanged(int position, String content) {
        // 修复：防止越界
        if (position < noList.size()) {
            noList.set(position, content);
        } else {
            // 如果越界，直接添加
            noList.add(content);
        }
        if (position == noList.size() - 1 && !content.trim().isEmpty()) {
            noList.add("");
            adapter.notifyItemInserted(noList.size() - 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNoList();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userCRUD.close();
    }
}