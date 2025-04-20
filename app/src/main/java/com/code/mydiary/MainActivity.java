package com.code.mydiary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DiaryDatabase dbHelper;
    private Toolbar toolbar;
    private TextView tabEntries, tabCalendar, tabDiary,mydiary_count;
    private FrameLayout containerEntries, containerCalendar, containerDiary;
    private ImageButton myBtnMenu, myBtnAdd;
    private ListView lv;
    private DiaryAdopter adopter;
    private List<Diary> diaryList = new ArrayList<>();
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        initToolbar();
        initTabs();
        initBottomButtons();
        initTV();
        initLV();
        refreshListView();
    }

    /**
     * 初始化 Toolbar
     */
    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
    }

    /**
     * 初始化标签页和 FrameLayout 页面容器（用于页面切换）
     */
    private void initTabs() {
        tabEntries = findViewById(R.id.tab_entries);
        tabCalendar = findViewById(R.id.tab_calendar);
        tabDiary = findViewById(R.id.tab_diary);

        containerEntries = findViewById(R.id.container_entries);
        containerCalendar = findViewById(R.id.container_calendar);
        containerDiary = findViewById(R.id.container_diary);

        tabEntries.setOnClickListener(v -> switchPage(0));
        tabCalendar.setOnClickListener(v -> switchPage(1));
        tabDiary.setOnClickListener(v -> switchPage(2));

        switchPage(0); // 默认显示 Entries 页
    }


    /**
     * 初始化底部菜单按钮与添加按钮
     */
    private void initBottomButtons() {
        myBtnMenu = findViewById(R.id.imgBt_menu);
        myBtnAdd = findViewById(R.id.imgBt_add);

        if (myBtnMenu != null) {
            myBtnMenu.setOnClickListener(v -> {
                Log.d("ClickDebug", "Menu按钮被点击");
                startActivity(new Intent(MainActivity.this, Menu.class));
            });
        }

        if (myBtnAdd != null) {
            myBtnAdd.setOnClickListener(v -> {
                Log.d("ClickDebug", "Add按钮被点击");
                Intent addIntent = new Intent(MainActivity.this, AddDiary.class);
                startActivityForResult(addIntent, 0);
            });
        }

        // 额外设置按钮（原 Setting 中跳转按钮）
        View btnSet1 = findViewById(R.id.btn_set1);
        if (btnSet1 != null) {
            btnSet1.setOnClickListener(v -> {
                Log.d("ClickDebug", "btu_set1按钮被点击");
                startActivity(new Intent(MainActivity.this, Menu.class));
            });
        }
    }


    private void initTV() {
        mydiary_count = findViewById(R.id.tv_diary_count);
    }

    private void initLV() {
        lv = findViewById(R.id.diary_lv);
        adopter = new DiaryAdopter(getApplicationContext(), diaryList);
        refreshListView();
        lv.setAdapter(adopter);
    }


    /**
     * 切换 FrameLayout 显示的页面
     */
    private void switchPage(int index) {
        containerEntries.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        containerCalendar.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        containerDiary.setVisibility(index == 2 ? View.VISIBLE : View.GONE);

        tabEntries.setSelected(index == 0);
        tabCalendar.setSelected(index == 1);
        tabDiary.setSelected(index == 2);
    }


    /**
     * 处理 Add日记页面,startActivityForResult返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String Title = data.getStringExtra("addDiary_title");
            String Body = data.getStringExtra("addDiary_body");
            String Time = data.getStringExtra("addDiary_time");
            Log.d(TAG, "onActivityResult: Title=" + Title + ", Body=" + Body + ", Time=" + Time);

            //String time, String weather, String temperature, String location, String title, String body, int mood, int tag
            Diary diary = new Diary(Time, "晴", "25", "江门", Title, Body, 1, 1);
            Log.d(TAG, "onActivityResult: 新建Diary=" + diary.toString());

            CRUD op = new CRUD(context);
            op.open();
            op.addDiary(diary);
            Log.d(TAG, "onActivityResult: addDiary已调用");
            op.close();
            refreshListView();


//            if (addDiaryTitle != null && !addDiaryTitle.isEmpty()) {
//                mytv_diary_title.setText(addDiaryTitle);
//            } else {
//                // 获取当前时间并格式化为字符串
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                String currentTime = sdf.format(new Date());
//                mytv_diary_title.setText(currentTime);
//                Log.d(TAG, "收到的日记标题为空，使用当前时间：" + currentTime);
//            }
//            mytv_diary_body.setText(addDiaryBody);
//        } else {
//            Log.d(TAG, "未收到有效的返回结果");
//        }
        }

    }

    public void refreshListView(){
        CRUD op = new CRUD(context);
        op.open();
        if(diaryList.size()>0)
            diaryList.clear();
//        diaryList.addAll(op.getAllDiary());
        List<Diary> allDiaries = op.getAllDiary();
        Log.d(TAG, "refreshListView: 查询到日记数量=" + allDiaries.size());
        mydiary_count.setText(String.valueOf(allDiaries.size()));
        diaryList.addAll(allDiaries);
        op.close();
        adopter.notifyDataSetChanged();
//        adopter.bockList = new ArrayList<>(diaryList);
    }
}
