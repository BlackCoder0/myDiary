package com.code.mydiary;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.code.mydiary.util.CRUD;
import com.code.mydiary.util.DiaryAdopter;
import com.code.mydiary.util.DiaryDatabase;
import com.code.mydiary.util.DiaryListItem;
import com.code.mydiary.util.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private DiaryDatabase dbHelper;
    private Diary diary;

    private ToastUtil toastUtil;
    private Toolbar toolbar;
    private TextView tabEntries, tabCalendar, tabDiary, mydiary_count; // {{ edit_1: 移除 diary_moon }}
    private FrameLayout containerEntries, containerCalendar, containerDiary;
    private ImageButton myBtnMenu, myBtnAdd;

    private ListView lv;
    private DiaryAdopter adopter;
    private List<Diary> diaryList = new ArrayList<>();
    private Context context = this;
    private List<DiaryListItem> diaryListItems = new ArrayList<>(); // 新数据源

    private LinearLayout searchBar;
    private EditText etSearch;
    private ImageButton btnSearchClose;
    private boolean isSearching = false;
    private List<DiaryListItem> originalDiaryListItems = new ArrayList<>(); // 保存原始数据

    private long currentUserId; // 成员变量


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        currentUserId = getIntent().getLongExtra("user_id", -1);
        if (currentUserId == -1) {
            toastUtil = new ToastUtil();
            toastUtil.showMsg(MainActivity.this, "用户ID无效，请重新登录");
            // finish();
            // return;
        }


        initToolbar();
        initTabs();
        initBottomButtons();
        initSearchBar(); // 新增
        initTV(); // {{ edit_2: initTV 不再需要初始化 diary_moon }}
        initLV();
        refreshListView(); // {{ edit_3: refreshListView 不再需要更新 diary_moon }}
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
        ImageButton myBtnSearch = findViewById(R.id.imgBt_search); // 新增

        if (myBtnMenu != null) {
            myBtnMenu.setOnClickListener(v -> {
                Log.d("ClickDebug", "Menu按钮被点击");
                Intent menuIntent = new Intent(this, Menu.class);
                menuIntent.putExtra("user_id", currentUserId);
                startActivity(menuIntent);
                // 新增：添加向右平移动画
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }

        if (myBtnAdd != null) {
            myBtnAdd.setOnClickListener(v -> {
                Log.d("ClickDebug", "Add按钮被点击");
                // === 新增逻辑：一天只能写一篇日记 ===
                String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

                CRUD op = new CRUD(context);
                op.open();
                List<Diary> allDiaries = op.getAllDiary();
                op.close();

                // 只查找属于当前用户的日记
                com.code.mydiary.util.UserCRUD userCRUD = new com.code.mydiary.util.UserCRUD(context);
                userCRUD.open();
                java.util.List<Long> userDiaryIds = new java.util.ArrayList<>();
                android.database.Cursor cursor = userCRUD.getUserDiaryIds(currentUserId);
                while (cursor.moveToNext()) {
                    int columnIndex = cursor.getColumnIndex(com.code.mydiary.util.UserDatabase.DIARY_ID);
                    if (columnIndex != -1) {
                        userDiaryIds.add(cursor.getLong(columnIndex));
                    }
                }
                cursor.close();
                userCRUD.close();

                Diary todayDiary = null;
                for (Diary diary : allDiaries) {
                    if (userDiaryIds.contains(diary.getId())) {
                        String diaryDate = diary.getTime();
                        if (diaryDate != null && diaryDate.length() >= 10 && diaryDate.substring(0, 10).equals(today)) {
                            todayDiary = diary;
                            break;
                        }
                    }
                }

                if (todayDiary != null) {
                    // 已有今日日记，进入编辑页面
                    Intent editIntent = new Intent(MainActivity.this, AddDiary.class);
                    editIntent.putExtra("id", todayDiary.getId());
                    editIntent.putExtra("time", todayDiary.getTime());
                    editIntent.putExtra("weather", todayDiary.getWeather());
                    editIntent.putExtra("temperature", todayDiary.getTemperature());
                    editIntent.putExtra("location", todayDiary.getLocation());
                    editIntent.putExtra("title", todayDiary.getTitle());
                    editIntent.putExtra("body", todayDiary.getBody());
                    editIntent.putExtra("mood", todayDiary.getMood());
                    editIntent.putExtra("tag", todayDiary.getTag());
                    editIntent.putExtra("mode", 3); // 编辑模式
                    startActivityForResult(editIntent, 1);
                    Log.d(TAG, "Add按钮：今日已有日记，进入编辑模式");
                } else {
                    // 没有今日日记，进入新建页面
                    Intent addIntent = new Intent(MainActivity.this, AddDiary.class);
                    addIntent.putExtra("mode", 4); // 4为创建笔记
                    startActivityForResult(addIntent, 0);
                    Log.d(TAG, "Add按钮：今日无日记，进入新建模式");
                }
            });

        }

        if (myBtnSearch != null) {
            myBtnSearch.setOnClickListener(v -> {
                if (!isSearching) {
                    enterSearchMode();
                }
            });
        }

    }


    private void initTV() {
        mydiary_count = findViewById(R.id.tv_diary_count);
        // {{ edit_4: 移除 diary_moon 的初始化 }}
    }

    private void initLV() {
        lv = findViewById(R.id.diary_lv);
        adopter = new DiaryAdopter(getApplicationContext(), diaryListItems);
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
    
        // --- 修改开始: 检查 resultCode ---
        // 如果用户取消了操作 (点击取消按钮或返回键)，则不执行任何操作
        if (resultCode != RESULT_OK) {
             Log.d(TAG, "onActivityResult: Operation canceled or no result (resultCode=" + resultCode + ")");
             return; // 直接返回，不刷新列表或处理数据
        }
        // --- 修改结束 ---
    
        // 只有在 resultCode 是 RESULT_OK 时才继续处理
        if (data != null && data.getExtras() != null) {
            int returnMode = data.getExtras().getInt("mode", -1);
            long diary_Id = data.getExtras().getLong("id", 0); // 获取 ID
    
            // --- 修改开始: 仅在 mode 为 0 或 1 时执行数据库操作和刷新 ---
            if (returnMode == 0 || returnMode == 1) {
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
                }
                // else { // returnMode 为 -1 的情况不再需要在这里处理数据库 }
    
                op.close();
                refreshListView(); // 只有在实际创建或更新后才刷新列表
            } else {
                 Log.d(TAG, "onActivityResult: No changes detected or required (mode=" + returnMode + ")");
                 // returnMode 为 -1，表示没有更改或用户未做任何有效输入，不需要刷新列表
            }
            // --- 修改结束 ---
    
        } else {
            // 虽然我们已经检查了 resultCode，但以防万一 data 为 null
            Log.d(TAG, "onActivityResult: RESULT_OK but data is null or has no extras.");
        }
    }


    public void refreshListView(){
        Log.d(TAG, "refreshListView: 开始刷新列表");
        long userId = currentUserId; // 只用成员变量
        CRUD op = new CRUD(context);
        op.open();
    
        // 新增：只查找属于当前用户的日记
        List<Long> userDiaryIds = new ArrayList<>();
        com.code.mydiary.util.UserCRUD userCRUD = new com.code.mydiary.util.UserCRUD(context);
        userCRUD.open();
        Cursor cursor = userCRUD.getUserDiaryIds(userId);
        while (cursor.moveToNext()) {
            // 注意：需要确保 UserDatabase.DIARY_ID 是正确的列名
            int columnIndex = cursor.getColumnIndex(com.code.mydiary.util.UserDatabase.DIARY_ID);
            if (columnIndex != -1) { // 检查列是否存在
                 userDiaryIds.add(cursor.getLong(columnIndex));
            } else {
                 Log.e(TAG, "Column UserDatabase.DIARY_ID not found in cursor.");
                 // 可以选择抛出异常或进行其他错误处理
            }
        }
        cursor.close();
        userCRUD.close();
    
        diaryList.clear();
        List<Diary> allDiaries = op.getAllDiary();
        for (Diary diary : allDiaries) {
            if (userDiaryIds.contains(diary.getId())) {
                diaryList.add(diary);
            }
        }
    
        // --- 按时间降序排序 (保持不变) ---
        Collections.sort(diaryList, new Comparator<Diary>() {
            @Override
            public int compare(Diary d1, Diary d2) {
                return d2.getTime().compareTo(d1.getTime());
            }
        });
        // --- 排序结束 ---
    
        Log.d(TAG, "refreshListView: 查询并排序后日记数量=" + diaryList.size());
    
        // 更新日记数量显示
        if (mydiary_count != null) {
            mydiary_count.setText(String.valueOf(diaryList.size()));
        }
    
        // === 关键补充：组装 diaryListItems ===
        diaryListItems.clear();
        String lastMonth = "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault());
        java.text.SimpleDateFormat displayFormat = new java.text.SimpleDateFormat("M", java.util.Locale.getDefault());
        for (Diary diary : diaryList) {
            String month = "";
            try {
                java.util.Date date = sdf.parse(diary.getTime().substring(0, 7));
                month = displayFormat.format(date);
            } catch (Exception e) {
                month = diary.getTime().substring(5, 7); // 兜底
            }
            if (!month.equals(lastMonth)) {
                diaryListItems.add(new com.code.mydiary.util.DiaryListItem(com.code.mydiary.util.DiaryListItem.TYPE_MONTH_HEADER, month, null));
                lastMonth = month;
            }
            diaryListItems.add(new com.code.mydiary.util.DiaryListItem(com.code.mydiary.util.DiaryListItem.TYPE_DIARY, null, diary));
        }
        // === 补充结束 ===
    
        op.close();
        if (adopter != null) {
            adopter.notifyDataSetChanged();
            Log.d(TAG, "refreshListView: 通知Adapter数据变化");
        } else {
            Log.e(TAG, "refreshListView: DiaryAdopter is null!");
            initLV();
        }
        Log.d(TAG, "refreshListView: 组装后的数据源大小=" + diaryListItems.size());
    }

    //搜索相关
    private void initSearchBar() {
        searchBar = findViewById(R.id.search_bar);
        etSearch = findViewById(R.id.et_search);
        btnSearchClose = findViewById(R.id.btn_search_close);

        btnSearchClose.setOnClickListener(v -> exitSearchMode());

        // 实时搜索：监听文本变化
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 只有输入不为空时才搜索，否则恢复原始数据
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    diaryListItems.clear();
                    diaryListItems.addAll(originalDiaryListItems);
                    if (adopter != null) adopter.notifyDataSetChanged();
                } else {
                    performSearch(keyword);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });
    }
    private void enterSearchMode() {
        isSearching = true;
        searchBar.setVisibility(View.VISIBLE);
        // 只请求焦点，不清空内容，避免触发TextWatcher导致列表被清空
        etSearch.requestFocus();
        // 保存原始数据（只保存一次，避免多次进入搜索模式时被覆盖）
        if (originalDiaryListItems.isEmpty()) {
            originalDiaryListItems.clear();
            originalDiaryListItems.addAll(diaryListItems);
        }
        // 弹出软键盘
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    private void exitSearchMode() {
        isSearching = false;
        searchBar.setVisibility(View.GONE);
        // 恢复原始数据
        diaryListItems.clear();
        diaryListItems.addAll(originalDiaryListItems);
        if (adopter != null) adopter.notifyDataSetChanged();
        // 清空搜索框内容
        etSearch.setText("");
        // 清空原始数据缓存，避免下次进入搜索模式时数据不对
        originalDiaryListItems.clear();
        // 收起软键盘
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }
    private void performSearch(String keyword) {
        if (keyword.isEmpty()) {
            // 若输入为空，显示全部
            diaryListItems.clear();
            diaryListItems.addAll(originalDiaryListItems);
            if (adopter != null) adopter.notifyDataSetChanged();
            return;
        }
        List<DiaryListItem> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (DiaryListItem item : originalDiaryListItems) {
            if (item.type == DiaryListItem.TYPE_DIARY && item.diary != null) {
                String title = item.diary.getTitle() == null ? "" : item.diary.getTitle();
                String body = item.diary.getBody() == null ? "" : item.diary.getBody();
                if (title.toLowerCase().contains(lowerKeyword) || body.toLowerCase().contains(lowerKeyword)) {
                    result.add(item);
                }
            }
        }
        diaryListItems.clear();
        diaryListItems.addAll(result);
        if (adopter != null) adopter.notifyDataSetChanged();
    }


    private static final int ID_DIARY_LV = R.id.diary_lv;  // 提取为常量

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        if (parentId == R.id.diary_lv) {
            // 先获取 DiaryListItem
            DiaryListItem item = (DiaryListItem) parent.getItemAtPosition(position);
            if (item.type == DiaryListItem.TYPE_DIARY && item.diary != null) {
                Diary curDiary = item.diary;
                ImfDiary imfDiary = new ImfDiary();
                imfDiary.showDiaryDialog(this, curDiary, new ImfDiary.DialogActionListener() {
                    @Override
                    public void onEditClicked(Diary diaryToEdit) {
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
                        showDeleteConfirmationDialog(diaryToDelete);
                    }
                });
            }
            // 如果是月份头部，什么都不做
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
