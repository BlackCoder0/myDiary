package com.code.mydiary;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import android.widget.LinearLayout;
import android.graphics.Color;

public class Menu extends AppCompatActivity {

    private LinearLayout layoutPhone, layoutDiary, layoutNo;
    private int selectedColor = Color.parseColor("#E0E0E0"); // 选中颜色
    private int normalColor = Color.TRANSPARENT; // 未选中颜色
    private long userId; // 新增：存储用户ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);
        Log.d("ClickDebug", "Menu Activity 启动");

        // 新增：获取从上一个 Activity 传来的 userId
        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            // 处理 userId 无效的情况，例如返回登录页或显示错误
            Toast.makeText(this, "用户ID无效，请重新登录", Toast.LENGTH_SHORT).show();
            // finish(); // 可以选择关闭当前页面
            // return;
        }


        layoutPhone = findViewById(R.id.layout_phone);
        layoutDiary = findViewById(R.id.layout_diary);
        layoutNo = findViewById(R.id.layout_no);

        layoutPhone.setOnClickListener(v -> {
            setSelected(layoutPhone);
            // 修改：传递 userId
            Intent intent = new Intent(this, SOSActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        layoutDiary.setOnClickListener(v -> {
            setSelected(layoutDiary);
            // 回到 MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_id", userId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        layoutNo.setOnClickListener(v -> {
            setSelected(layoutNo);
            // 修改：传递 userId
            Intent intent = new Intent(this, NoListActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
    }

    private void setSelected(LinearLayout selected) {
        layoutPhone.setBackgroundColor(normalColor);
        layoutDiary.setBackgroundColor(normalColor);
        layoutNo.setBackgroundColor(normalColor);
        selected.setBackgroundColor(selectedColor);
    }
}