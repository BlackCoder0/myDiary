package com.code.mydiary.util;

import android.view.View;
import android.widget.TextView;
import androidx.viewpager.widget.ViewPager;

public class TabLayout {
    private static final String TAG = "TabLayout";
    private TextView tabEntries, tabCalendar, tabDairy;
    private ViewPager viewPager;

    public TabLayout(TextView tabEntries, TextView tabCalendar, TextView tabDairy, ViewPager viewPager) {
        this.tabEntries = tabEntries;
        this.tabCalendar = tabCalendar;
        this.tabDairy = tabDairy;
        this.viewPager = viewPager;
    }

    public void initTabs() {
        // 设置标签点击事件
        tabEntries.setOnClickListener(v -> {
            viewPager.setCurrentItem(0);
            updateTabSelection(0);
        });

        tabCalendar.setOnClickListener(v -> {
            viewPager.setCurrentItem(1);
            updateTabSelection(1);
        });

        tabDairy.setOnClickListener(v -> {
            viewPager.setCurrentItem(2);
            updateTabSelection(2);
        });

        // 设置 ViewPager 页面切换监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // 不需要实现
            }

            @Override
            public void onPageSelected(int position) {
                updateTabSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 不需要实现
            }
        });

        // 设置初始选中的标签
        updateTabSelection(0);
    }

    private void updateTabSelection(int position) {
        // 重置所有标签的状态
        tabEntries.setSelected(false);
        tabCalendar.setSelected(false);
        tabDairy.setSelected(false);

        // 设置选中标签的状态
        switch (position) {
            case 0:
                tabEntries.setSelected(true);
                break;
            case 1:
                tabCalendar.setSelected(true);
                break;
            case 2:
                tabDairy.setSelected(true);
                break;
        }
    }
}