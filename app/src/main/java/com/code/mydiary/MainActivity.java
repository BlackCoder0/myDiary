package com.code.mydiary;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import com.code.mydiary.util.CRUD;
import com.code.mydiary.util.DiaryAdopter;
import com.code.mydiary.util.DiaryDatabase;

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

                newDiary = op.addDiary(newDiary); // 获取带ID的Diary对象

                // 关键：建立用户-日记关联
                long userId = getIntent().getLongExtra("user_id", -1);
                com.code.mydiary.util.UserCRUD userCRUD = new com.code.mydiary.util.UserCRUD(context);
                userCRUD.open();
                userCRUD.linkUserDiary(userId, newDiary.getId());
                userCRUD.close();
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
        long userId = getIntent().getLongExtra("user_id", -1);
        CRUD op = new CRUD(context);
        op.open();
    
        // 新增：只查找属于当前用户的日记
        List<Long> userDiaryIds = new ArrayList<>();
        com.code.mydiary.util.UserCRUD userCRUD = new com.code.mydiary.util.UserCRUD(context);
        userCRUD.open();
        Cursor cursor = userCRUD.getUserDiaryIds(userId);
        while (cursor.moveToNext()) {
            userDiaryIds.add(cursor.getLong(cursor.getColumnIndex(com.code.mydiary.util.UserDatabase.DIARY_ID)));
        }
        cursor.close();
        userCRUD.close();
    
        diaryList.clear();
        List<Diary> allDiaries = op.getAllDiary(); // 只定义一次
        for (Diary diary : allDiaries) {
            if (userDiaryIds.contains(diary.getId())) {
                diaryList.add(diary);
            }
        }
        Log.d(TAG, "refreshListView: 查询到日记数量=" + diaryList.size());
        // 确保 mydiary_count 不为 null
        if (mydiary_count != null) {
            mydiary_count.setText(String.valueOf(diaryList.size())); // 更新日记数量显示
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
            // --- 修改开始 ---
            // 不再直接启动 AddDiary，而是显示预览对话框
            ImfDiary imfDiary = new ImfDiary();
            imfDiary.showDiaryDialog(this, curDiary, new ImfDiary.DialogActionListener() {
                @Override
                public void onEditClicked(Diary diaryToEdit) {
                    // 从预览页面点击编辑按钮后，启动 AddDiary
                    Intent intent = new Intent(MainActivity.this, AddDiary.class);
                    intent.putExtra("id", diaryToEdit.getId());
                    intent.putExtra("time", diaryToEdit.getTime());
                    intent.putExtra("weather", diaryToEdit.getWeather());
                    intent.putExtra("temperature", diaryToEdit.getTemperature());
                    intent.putExtra("location", diaryToEdit.getLocation());
                    intent.putExtra("title", diaryToEdit.getTitle());
                    intent.putExtra("body", diaryToEdit.getBody());
                    intent.putExtra("mood", diaryToEdit.getMood());
                    intent.putExtra("tag", diaryToEdit.getTag());
                    intent.putExtra("mode", 3); // 3 为修改模式
                    startActivityForResult(intent, 1); // 使用请求码 1
                    Log.d(TAG, "onItemClick: Starting AddDiary from ImfDiary for editing diary ID: " + diaryToEdit.getId());
                }

                @Override
                public void onDeleteClicked(Diary diaryToDelete) {
                    // 从预览页面点击删除按钮后，执行删除操作并刷新列表
                    showDeleteConfirmationDialog(diaryToDelete);
                }
            });
            // --- 修改结束 ---
            // 下面的旧代码被注释或删除
            /*
            Intent intent = new Intent(MainActivity.this, AddDiary.class);
            // ... (传递 extras 的旧代码) ...
            intent.putExtra("mode", 3); // 3 为修改模式
            startActivityForResult(intent,1); // 使用请求码 1
            Log.d(TAG,"onItemClick: Starting AddDiary for editing diary ID: " + curDiary.getId());
            */
        }
    }

    // 新增：显示删除确认对话框的方法
    private void showDeleteConfirmationDialog(final Diary diaryToDelete) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除这份回忆吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    CRUD op = new CRUD(context);
                    op.open();
                    op.deleteDiary(diaryToDelete);
                    op.close();
                    refreshListView(); // 删除后刷新列表
                    Log.d(TAG, "Diary deleted: ID " + diaryToDelete.getId());
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
