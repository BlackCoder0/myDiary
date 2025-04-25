package com.code.mydiary;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
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

        initSexChange();
    }

    private void setSelected(LinearLayout selected) {
        layoutPhone.setBackgroundColor(normalColor);
        layoutDiary.setBackgroundColor(normalColor);
        layoutNo.setBackgroundColor(normalColor);
        selected.setBackgroundColor(selectedColor);
    }

    //根据性别更新
    private void initSexChange() {
        // 动态设置顶部背景
        ImageView imageView = findViewById(R.id.imageView);
        if (imageView != null) {
            imageView.setImageResource(
                    com.code.mydiary.util.GenderResourceUtil.getMenuBackgroundRes(this)
            );
        }

        // 获取主色
        int mainColor = getResources().getColor(
                com.code.mydiary.util.GenderResourceUtil.getTabMainColorRes(this)
        );

        // 设置layout_phone、layout_diary、layout_no的图标和文字颜色
        int[] iconIds = {R.id.menu_phone_img, R.id.menu_diary_img, R.id.menu_NO_img};
        int[] textIds = {R.id.menu_phone_text, R.id.menu_diary_text, R.id.menu_NO_text,R.id.menu_JT1,R.id.menu_JT2,R.id.menu_JT3};
        int[] LineIds = {R.id.menu_line1,R.id.menu_line2,R.id.menu_line3,};


        for (int iconId : iconIds) {
            ImageView icon = findViewById(iconId);
            if (icon != null) icon.setColorFilter(mainColor);
        }
        for (int textId : textIds) {
            TextView text = findViewById(textId);
            if (text != null) text.setTextColor(mainColor);
        }
        for (int lineId : LineIds) {
            ImageView image = findViewById(lineId);
            if (image != null) image.setBackgroundColor(mainColor);
        }
    }

}