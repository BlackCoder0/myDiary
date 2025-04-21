package com.code.mydiary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private DiaryDatabase dbHelper;
    private Diary diary;
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
        lv.setOnItemClickListener(this);
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
            int Weather = data.getIntExtra("addDiary_weather", -1);
            int Mood = data.getIntExtra("addDiary_mood", -1);
            int Mode = data.getIntExtra("addDiary_mode", 1);

            Log.d(TAG, "onActivityResult: Title=" + Title + ", Body=" + Body + ", Time=" + Time + ", Weather=" + Weather + ", Mood=" + Mood);

            Diary diary = new Diary(Time, Weather, "25", "江门", Title, Body, Mood, 1, Mode);

            CRUD op = new CRUD(context);
            op.open();
            op.addDiary(diary);
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

    private static final int ID_DIARY_LV = R.id.diary_lv;  // 提取为常量

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        if (parentId == R.id.diary_lv) {
            Diary curDiary = (Diary) parent.getItemAtPosition(position);
//                Intent intent = new Intent(MainActivity.this, ImfDiary.class);
                Intent intent = new Intent(MainActivity.this, AddDiary.class);

                //String time, int weather, String temperature, String location, String title, String body, int mood, int tag, int mode
                intent.putExtra("id",curDiary.getId());
                intent.putExtra("time",curDiary.getTime());
                intent.putExtra("weather",curDiary.getWeather());
                intent.putExtra("temperature",curDiary.getTemperature());
                intent.putExtra("location",curDiary.getLocation());
                intent.putExtra("title",curDiary.getTitle());
                intent.putExtra("body",curDiary.getBody());
                intent.putExtra("mood",curDiary.getMood());
                intent.putExtra("tag",curDiary.getTag());
                intent.putExtra("mode", 3);
                startActivityForResult(intent,1);
                Log.d(TAG,"onItemClick:"+position);
                //break;

        }
    }
}
