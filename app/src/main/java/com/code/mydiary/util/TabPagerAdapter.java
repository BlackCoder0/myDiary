package com.code.mydiary.util;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.code.mydiary.ContentFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "TabPagerAdapter";

    public TabPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int position) {
        return ContentFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 3; // 三个标签页
    }
}