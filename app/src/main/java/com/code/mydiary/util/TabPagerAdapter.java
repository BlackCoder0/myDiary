package com.code.mydiary.util;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.code.mydiary.ContentFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "TabPagerAdapter";

    public TabPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // 使用现有的 ContentFragment 类，传入不同的页面类型
        return ContentFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 3;
    }
}