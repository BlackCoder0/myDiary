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
                addIntent.putExtra("mode",4);//4为创建笔记
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
        super.onActivityResult(requestCode, resultCode, data); // 先调用父类方法

        // 检查返回结果是否有效
        if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            int returnMode = data.getExtras().getInt("mode", -1);
            long diary_Id = data.getExtras().getLong("id", 0); // 获取 ID

            CRUD op = new CRUD(context);
            op.open();

            if (returnMode == 1) {  // update current note
                Log.d(TAG, "onActivityResult: Updating diary ID: " + diary_Id);
                // 从返回的 Intent 中获取所有更新后的数据
                String title = data.getStringExtra("title");
                String body = data.getStringExtra("body");
                String time = data.getStringExtra("time");
                String temperature = data.getStringExtra("temperature");
                String location = data.getStringExtra("location");
                int weather = data.getIntExtra("weather", -1);
                int mood = data.getIntExtra("mood", -1);
                int tag = data.getIntExtra("tag", 1);
                // 注意：mode 本身不需要存入 Diary 对象，它是操作类型

                // 创建包含所有更新信息的 Diary 对象
                Diary updatedDiary = new Diary(time, weather, temperature, location, title, body, mood, tag, 1); // mode 存 1 或其他默认值
                updatedDiary.setId(diary_Id); // 设置要更新的日记的 ID

                op.updataDiary(updatedDiary); // 更新数据库

            } else if (returnMode == 0) {  // create new note
                Log.d(TAG, "onActivityResult: Creating new diary");
                // 从返回的 Intent 中获取所有新数据
                String title = data.getStringExtra("title");
                String body = data.getStringExtra("body");
                String time = data.getStringExtra("time");
                String temperature = data.getStringExtra("temperature");
                String location = data.getStringExtra("location");
                int weather = data.getIntExtra("weather", -1);
                int mood = data.getIntExtra("mood", -1);
                int tag = data.getIntExtra("tag", 1);
                 // 注意：mode 本身不需要存入 Diary 对象

                // 创建新的 Diary 对象
                Diary newDiary = new Diary(time, weather, temperature, location, title, body, mood, tag, 1); // mode 存 1 或其他默认值

                op.addDiary(newDiary); // 添加到数据库
            } else {
                 Log.d(TAG, "onActivityResult: No changes detected (mode=" + returnMode + ")");
                 // returnMode 为 -1，表示没有更改或用户未做任何有效输入
            }

            op.close();
            refreshListView(); // 刷新列表以显示更改

        } else {
            Log.d(TAG, "onActivityResult: Invalid result or no data returned (resultCode=" + resultCode + ")");
        }
    }


    public void refreshListView(){
        CRUD op = new CRUD(context);
        op.open();
        // 优化：先清除旧数据，再添加新数据
        if(diaryList != null) { // 添加空指针检查
             diaryList.clear();
             List<Diary> allDiaries = op.getAllDiary();
             Log.d(TAG, "refreshListView: 查询到日记数量=" + allDiaries.size());
             mydiary_count.setText(String.valueOf(allDiaries.size())); // 更新日记数量显示
             diaryList.addAll(allDiaries); // 添加所有从数据库查询到的日记
        } else {
            diaryList = new ArrayList<>(); // 如果 diaryList 为 null，则初始化
            List<Diary> allDiaries = op.getAllDiary();
            Log.d(TAG, "refreshListView: 初始化 diaryList 并查询到日记数量=" + allDiaries.size());
            mydiary_count.setText(String.valueOf(allDiaries.size())); // 更新日记数量显示
            diaryList.addAll(allDiaries);
        }
        op.close();
        if (adopter != null) { // 添加空指针检查
            adopter.notifyDataSetChanged(); // 通知适配器数据已更改
        } else {
            // 如果 adopter 为 null，可能需要重新初始化
             Log.e(TAG, "refreshListView: DiaryAdopter is null!");
             initLV(); // 尝试重新初始化 ListView 和 Adopter
        }
    }

    private static final int ID_DIARY_LV = R.id.diary_lv;  // 提取为常量

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        if (parentId == R.id.diary_lv) {
            Diary curDiary = (Diary) parent.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, AddDiary.class);

            // 传递所有日记字段到 AddDiary
            intent.putExtra("id",curDiary.getId());
            intent.putExtra("time",curDiary.getTime());
            intent.putExtra("weather",curDiary.getWeather());
            intent.putExtra("temperature",curDiary.getTemperature());
            intent.putExtra("location",curDiary.getLocation());
            intent.putExtra("title",curDiary.getTitle());
            intent.putExtra("body",curDiary.getBody());
            intent.putExtra("mood",curDiary.getMood());
            intent.putExtra("tag",curDiary.getTag());
            intent.putExtra("mode", 3); // 3 为修改模式
            startActivityForResult(intent,1); // 使用请求码 1
            Log.d(TAG,"onItemClick: Starting AddDiary for editing diary ID: " + curDiary.getId());
        }
    }
}
