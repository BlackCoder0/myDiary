package com.code.mydiary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView tabEntries, tabCalendar, tabDiary;
    private FrameLayout containerEntries,containerCalendar, containerDiary;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // 初始化 tab 控件
        tabEntries = findViewById(R.id.tab_entries);
        tabCalendar = findViewById(R.id.tab_calendar);
        tabDiary = findViewById(R.id.tab_diary);

        // 初始化内容容器
        containerEntries = findViewById(R.id.container_entries);
        containerCalendar = findViewById(R.id.container_calendar);
        containerDiary = findViewById(R.id.container_diary);

        // 设置点击事件
        tabEntries.setOnClickListener(v -> switchPage(0));
        tabCalendar.setOnClickListener(v -> switchPage(1));
        tabDiary.setOnClickListener(v -> switchPage(2));

        // 默认显示 Entries 页面
        switchPage(0);
    }

    private void switchPage(int index) {
        containerEntries.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        containerCalendar.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        containerDiary.setVisibility(index == 2 ? View.VISIBLE : View.GONE);

        // 恢复所有 tab 的背景
        tabEntries.setSelected(false);
        tabCalendar.setSelected(false);
        tabDiary.setSelected(false);

        // 设置选中 tab 的背景
        if (index == 0) tabEntries.setSelected(true);
        else if (index == 1) tabCalendar.setSelected(true);
        else if (index == 2) tabDiary.setSelected(true);
    }

}
