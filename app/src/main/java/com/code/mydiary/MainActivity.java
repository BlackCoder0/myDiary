package com.code.mydiary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.code.mydiary.util.TabLayout;
import com.code.mydiary.util.TabPagerAdapter;

public class MainActivity extends AppCompatActivity {
    final String TAG = "test";
    private TextView tabEntries, tabCalendar, tabDiary;
    private ImageButton myBtnMenu, myBtnAdd;
    private FrameLayout containerEntries,containerCalendar, containerDiary;
    private ViewPager viewPager;

    private ContentFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main_activity);
//        Intent intent = new Intent(this, SelectByText.class);
//        startActivity(intent);
//        finish();

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

        // 初始化 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

//        // 初始化标签
//        tabEntries = findViewById(R.id.tab_entries);
//        tabCalendar = findViewById(R.id.tab_calendar);
//        tabDairy = findViewById(R.id.tab_diary);

        // 初始化 ViewPager
        viewPager = findViewById(R.id.view_pager);
        if (viewPager != null) {
            TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(0); // 禁用缓存

            // 设置 ViewPager 的页面变化监听器
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                @Override
                public void onPageSelected(int position) {
                    updateCurrentFragment(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {}
            });
        }

//        // 初始化 TabLayout
//        if (tabEntries != null && tabCalendar != null && tabDiary != null && viewPager != null) {
//            TabLayout tabLayout = new TabLayout(tabEntries, tabCalendar, tabDiary, viewPager);
//            tabLayout.initTabs();
//        }

        // 设置按钮的点击事件
        setButtonListeners();
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


private void updateCurrentFragment(int position) {
    if (viewPager != null) {
        currentFragment = (ContentFragment) viewPager.getAdapter().instantiateItem(viewPager, position);
    }
}
    private void setButtonListeners() {
        myBtnMenu = findViewById(R.id.imgBt_menu);
        myBtnAdd = findViewById(R.id.imgBt_add);

        if (myBtnMenu != null) {
            myBtnMenu.setOnClickListener(v -> {
                Log.d("ClickDebug", "Menu按钮被点击");
                // 跳转到 MenuActivity
                Intent menuIntent = new Intent(MainActivity.this, Menu.class);
                startActivity(menuIntent);
            });
        }

        if (myBtnAdd != null) {
            myBtnAdd.setOnClickListener(v -> {
                Log.d("ClickDebug", "Add按钮被点击");
                // 跳转到 EditDiaryActivity
                Intent addIntent = new Intent(MainActivity.this, EditDiary.class);
                startActivityForResult(addIntent, 0);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String addDiary = data.getStringExtra("input");
            if (addDiary != null) {
                Log.d(TAG, "收到的日记内容: " + addDiary);
            } else {
                Log.d(TAG, "收到的日记内容为空");
            }
        } else {
            Log.d(TAG, "未收到有效的返回结果");
        }
    }
}
