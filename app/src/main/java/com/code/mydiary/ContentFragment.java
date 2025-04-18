package com.code.mydiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class ContentFragment extends Fragment {
    private static final String TAG = "ContentFragment";
    private static final String ARG_PAGE_TYPE = "page_type";
    private View rootView;
    private ImageButton imgBtMenu, imgBtAdd;

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
        View view = null;

        switch (pageType) {
            case 0: // Entries 页面
                view = inflater.inflate(R.layout.select_by_text_activity, container, false);
                imgBtMenu = view.findViewById(R.id.imgBt_menu);
                imgBtAdd = view.findViewById(R.id.imgBt_add);

                // 设置按钮的点击事件监听器
                if (imgBtMenu != null) {
                    imgBtMenu.setOnClickListener(v -> {
                        // 跳转到 MenuActivity
                        Toast.makeText(getActivity(), "点击了菜单按钮2", Toast.LENGTH_SHORT).show();
                        Intent menuIntent = new Intent(getActivity(), Menu.class);
                        startActivity(menuIntent);
                    });
                }

                if (imgBtAdd != null) {
                    imgBtAdd.setOnClickListener(v -> {
                        // 跳转到 EditDiaryActivity
                        Toast.makeText(getActivity(), "点击了日记按钮2", Toast.LENGTH_SHORT).show();
                        Intent addIntent = new Intent(getActivity(), EditDiary.class);
                        startActivity(addIntent);
                    });
                }
                break;
            case 1: // Calendar 页面
                view = inflater.inflate(R.layout.select_by_day_activity, container, false);
                break;
            case 2: // Dairy 页面
                view = inflater.inflate(R.layout.setting_activity, container, false);
                break;
            default:
                view = inflater.inflate(R.layout.select_by_text_activity, container, false);
                imgBtMenu = view.findViewById(R.id.imgBt_menu);
                imgBtAdd = view.findViewById(R.id.imgBt_add);
                break;
        }

        rootView = view;
        return view;
    }

    public ImageButton getImgBtMenu() {
        return imgBtMenu;
    }

    public ImageButton getImgBtAdd() {
        return imgBtAdd;
    }
}