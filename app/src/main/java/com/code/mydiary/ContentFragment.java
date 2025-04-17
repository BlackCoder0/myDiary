package com.code.mydiary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class ContentFragment extends Fragment {
    private static final String TAG = "ContentFragment";
    private static final String ARG_PAGE_TYPE = "page_type";

    public static ContentFragment newInstance(int pageType) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_TYPE, pageType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int pageType = getArguments().getInt(ARG_PAGE_TYPE, 0);

        // 根据页面类型加载不同的布局
        View view;
        switch (pageType) {
            case 0: // Entries 页面
                view = inflater.inflate(R.layout.fragment_entries, container, false);
                break;
            case 1: // Calendar 页面
                view = inflater.inflate(R.layout.fragment_calendar, container, false);
                break;
            case 2: // Dairy 页面
                view = inflater.inflate(R.layout.fragment_dairy, container, false);
                break;
            default:
                view = inflater.inflate(R.layout.fragment_entries, container, false);
                break;
        }

        return view;
    }
}