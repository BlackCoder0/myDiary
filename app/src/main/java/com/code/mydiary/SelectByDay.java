package com.code.mydiary;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.code.mydiary.util.TabLayout;
import com.code.mydiary.util.TabPagerAdapter;

public class SelectByDay extends AppCompatActivity {

    private TextView tabEntries, tabCalendar, tabDairy;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_by_day_activity);

        // 初始化 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 初始化标签
        tabEntries = findViewById(R.id.tab_entries);
        tabCalendar = findViewById(R.id.tab_calendar);
        tabDairy = findViewById(R.id.tab_dairy);

        // 初始化 ViewPager
        viewPager = findViewById(R.id.view_pager);
        TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        // 初始化 TabLayout
        TabLayout tabLayout = new TabLayout(tabEntries, tabCalendar, tabDairy, viewPager);
        tabLayout.initTabs();
    }
}