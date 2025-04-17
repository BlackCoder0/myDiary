package com.code.mydiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import com.code.mydiary.util.TabLayout;
import com.code.mydiary.util.TabPagerAdapter;

import java.util.Objects;

public class SelectByText extends AppCompatActivity {
    private TextView tabEntries, tabCalendar, tabDairy;
    private ImageButton myBtnMenu, myBtnAdd;
    private ViewPager viewPager;
    private ContentFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_by_text_activity);

        // 初始化 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        // 初始化标签
        tabEntries = findViewById(R.id.tab_entries);
        tabCalendar = findViewById(R.id.tab_calendar);
        tabDairy = findViewById(R.id.tab_dairy);

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

        // 初始化 TabLayout
        if (tabEntries != null && tabCalendar != null && tabDairy != null && viewPager != null) {
            TabLayout tabLayout = new TabLayout(tabEntries, tabCalendar, tabDairy, viewPager);
            tabLayout.initTabs();
        }

        // 设置按钮的点击事件
        setButtonListeners();
    }

    private void updateCurrentFragment(int position) {
        if (viewPager != null) {
            currentFragment = (ContentFragment) viewPager.getAdapter().instantiateItem(viewPager, position);
        }
    }

    private void setButtonListeners() {
        if (currentFragment != null) {
            ImageButton imgBtMenu = currentFragment.getImgBtMenu();
            ImageButton imgBtAdd = currentFragment.getImgBtAdd();

            if (imgBtMenu != null) {
                imgBtMenu.setOnClickListener(v -> {
                    // 跳转到 MenuActivity
                    Intent menuIntent = new Intent(SelectByText.this, Menu.class);
                    startActivity(menuIntent);
                });
            }

            if (imgBtAdd != null) {
                imgBtAdd.setOnClickListener(v -> {
                    // 跳转到 EditDiaryActivity
                    Intent addIntent = new Intent(SelectByText.this, EditDiary.class);
                    startActivity(addIntent);
                });
            }
        }
    }
}
