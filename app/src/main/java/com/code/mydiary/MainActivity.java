package com.code.mydiary;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.code.mydiary.util.BlueDotDecorator;
import com.code.mydiary.util.CRUD;

import com.code.mydiary.util.DiaryAdopter;
import com.code.mydiary.util.DiaryDatabase;
import com.code.mydiary.util.DiaryListItem;
import com.code.mydiary.util.GenderResourceUtil;
import com.code.mydiary.util.YellowDotDecorator;
import com.code.mydiary.util.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.code.mydiary.util.UserCRUD;
import com.code.mydiary.util.UserDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private boolean isCalendarInitialized = false;//日历初始化
    private MaterialCalendarView calendarView; // 日历控件成员变量

    private Toolbar toolbar;
    private TextView tabEntries, tabCalendar, tabDiary, mydiary_count;
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
    private LinearLayout setChangePwd, setAway; // 已有
    private long lastBackPressedTime = 0;

    private static final String BACKUP_FILE = "backup_diaries.json";//时光倒流时候的存储

    /**
     * 判断是否处于时光倒流模式
     * @return true表示处于时光倒流模式，false表示不处于
     */
    private boolean isTimeReverse() {
        return getIntent().getBooleanExtra("restoreDiaries", false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 自动登录检查
        SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
        long savedUserId = sp.getLong("user_id", -1);
        if (savedUserId != -1) {
            currentUserId = savedUserId;
        } else {
            currentUserId = getIntent().getLongExtra("user_id", -1);
            if (currentUserId != -1) {
                sp.edit().putLong("user_id", currentUserId).apply();
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);



//        currentUserId = getIntent().getLongExtra("user_id", -1);
        if (currentUserId == -1) {
            ToastUtil.showMsg(MainActivity.this, "用户ID无效，请重新登录");
            // finish();
            // return;
        }

        if (getIntent().getBooleanExtra("restoreDiaries", false)) {
            restoreDiaries();
            // 切换回Entries页面
            switchPage(0);
        }
        if (hasBackupFile() && !getIntent().getBooleanExtra("restoreDiaries", false)) {
            restoreDiaries();
            switchPage(0);
        }
        initToolbar();
        initTabs();
        initBottomButtons();
        initSearchBar();
        initTV();
        initLV();
        initSettingPage();
        initSexChange();
        refreshListView();
        setupCalendarDecorators();
        setupEditDayButton();
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
        ImageButton myBtnSearch = findViewById(R.id.imgBt_search); 

        if (myBtnMenu != null) {
            myBtnMenu.setOnClickListener(v -> {
                Log.d("ClickDebug", "Menu按钮被点击");
                Intent menuIntent = new Intent(this, Menu.class);
                menuIntent.putExtra("user_id", currentUserId);
                startActivity(menuIntent);
                // 添加向右平移动画
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }

        if (myBtnAdd != null) {
            myBtnAdd.setOnClickListener(v -> AddButtonClick()); 
        }

        if (myBtnSearch != null) {
            myBtnSearch.setOnClickListener(v -> {
                if (!isSearching) {
                    enterSearchMode();
                }
            });
        }

    }

    /**
     * 处理添加按钮点击事件
     */
    private void AddButtonClick() {
        Log.d("ClickDebug", "Add按钮被点击");
        // 一天只能写一篇日记 
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

        CRUD op = new CRUD(context);
        op.open();
        List<Diary> allDiaries = op.getAllDiary();
        op.close();

        // 只查找属于当前用户的日记
        UserCRUD userCRUD = new UserCRUD(context);
        userCRUD.open();
        java.util.List<Long> userDiaryIds = new java.util.ArrayList<>();
        Cursor cursor = userCRUD.getUserDiaryIds(currentUserId);
        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndex(UserDatabase.DIARY_ID);
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
    }


    private void initTV() {
        mydiary_count = findViewById(R.id.tv_diary_count);
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
        // 每次切换页面时重新设置背景
        initSexChange();

        // 切换到 Calendar 页时刷新日历装饰
        if (index == 1) {
            setupCalendarDecorators();
        }
    }


    /**
     * 处理 Add日记页面,startActivityForResult返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // 先调用父类方法

        // 如果用户取消了操作 (点击取消按钮或返回键)，则不执行任何操作
        if (resultCode != RESULT_OK) {
            Log.d(TAG, "onActivityResult: Operation canceled or no result (resultCode=" + resultCode + ")");
            return; // 直接返回，不刷新列表或处理数据
        }

        // 只有在 resultCode 是 RESULT_OK 时才继续处理
        if (data != null && data.getExtras() != null) {
            int returnMode = data.getExtras().getInt("mode", -1);
            long diaryId = data.getExtras().getLong("id", 0); // 获取 ID

            // 仅在 mode 为 0 或 1 时执行数据库操作和刷新
            if (returnMode == 0 || returnMode == 1) {
                Diary diaryData = GetDiaryData(data);
                if (diaryData == null) {
                    Log.e(TAG, "onActivityResult: Failed to extract diary data from intent.");
                    return;
                }

                if (returnMode == 1) {  // update current note
                    UpdateDiary(diaryData, diaryId);
                } else if (returnMode == 0) {  // create new note
                    CreateDiary(diaryData);
                }
                refreshListView(); // 刷新列表
            } else {
                Log.d(TAG, "onActivityResult: No changes detected or required (mode=" + returnMode + ")");
            }
        } else {
            Log.d(TAG, "onActivityResult: RESULT_OK but data is null or has no extras.");
        }
    }

    /**
     * 从 Intent 中提取日记数据
     * @param data Intent 数据
     * @return 包含日记数据的 Diary 对象，如果数据无效则返回 null
     */
    private Diary GetDiaryData(Intent data) {
        if (data == null || data.getExtras() == null) {
            return null;
        }
        String title = data.getStringExtra("title");
        String body = data.getStringExtra("body");
        String time = data.getStringExtra("time");
        String temperature = data.getStringExtra("temperature");
        String location = data.getStringExtra("location");
        int weather = data.getIntExtra("weather", -1);
        int mood = data.getIntExtra("mood", -1);
        int tag = data.getIntExtra("tag", 1);
        // 注意：mode 本身不需要存入 Diary 对象
        return new Diary(time, weather, temperature, location, title, body, mood, tag, 1); // mode 存 1 或其他默认值
    }

    /**
     * 处理创建新日记的结果
     * @param newDiary 从 Intent 中提取的新日记数据
     */
    private void CreateDiary(Diary newDiary) {
        Log.d(TAG, "onActivityResult: Creating new diary");
        CRUD op = new CRUD(context);
        op.open();
        Diary createdDiary = op.addDiary(newDiary); // 获取带ID的Diary对象
        op.close();

        // 关键：建立用户-日记关联
        // long userId = getIntent().getLongExtra("user_id", -1); // 避免重复获取
        if (currentUserId != -1 && createdDiary != null) {
            UserCRUD userCRUD = new UserCRUD(context);
            userCRUD.open();
            userCRUD.linkUserDiary(currentUserId, createdDiary.getId());
            userCRUD.close();
        } else {
            Log.e(TAG, "CreateDiary: Invalid user ID or created diary is null.");
        }
    }

    /**
     * 处理更新现有日记的结果
     * @param updatedDiary 从 Intent 中提取的更新后的日记数据
     * @param diaryId 要更新的日记 ID
     */
    private void UpdateDiary(Diary updatedDiary, long diaryId) {
        Log.d(TAG, "onActivityResult: Updating diary ID: " + diaryId);
        updatedDiary.setId(diaryId); // 设置要更新的日记的 ID

        CRUD op = new CRUD(context);
        op.open();
        op.updataDiary(updatedDiary); // 更新数据库
        op.close();
    }


    public void refreshListView() {
        Log.d(TAG, "refreshListView: 开始刷新列表");
        long userId = currentUserId; // 只用成员变量
        CRUD op = new CRUD(context);
        op.open();

        // 只查找属于当前用户的日记
        List<Long> userDiaryIds = new ArrayList<>();
        UserCRUD userCRUD = new UserCRUD(context);
        userCRUD.open();
        Cursor cursor = userCRUD.getUserDiaryIds(userId);
        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndex(UserDatabase.DIARY_ID);
            if (columnIndex != -1) { // 检查列是否存在
                userDiaryIds.add(cursor.getLong(columnIndex));
            } else {
                Log.e(TAG, "Column UserDatabase.DIARY_ID not found in cursor.");
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

        // 组装 diaryListItems
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
                diaryListItems.add(new DiaryListItem(DiaryListItem.TYPE_MONTH_HEADER, month, null));
                lastMonth = month;
            }
            diaryListItems.add(new DiaryListItem(DiaryListItem.TYPE_DIARY, null, diary));
        }
        // === 补充结束 ===
        groupDiariesByMonth(); // 调用新方法组装列表项

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

      /**
       * 将排序后的日记按月份分组，并填充 diaryListItems
       */
      private void groupDiariesByMonth() {
          diaryListItems.clear();
          String lastMonth = "";
          java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault());
          java.text.SimpleDateFormat displayFormat = new java.text.SimpleDateFormat("M", java.util.Locale.getDefault());
          for (Diary diary : diaryList) {
              String month = "";
              try {
                  // 确保 diary.getTime() 不为 null 且长度足够
                  if (diary.getTime() != null && diary.getTime().length() >= 7) {
                      java.util.Date date = sdf.parse(diary.getTime().substring(0, 7));
                      if (date != null) {
                          month = displayFormat.format(date);
                      } else {
                          month = diary.getTime().substring(5, 7); // 解析失败兜底
                      }
                  } else {
                      Log.w(TAG, "groupDiariesByMonth: Invalid time format for diary ID: " + diary.getId());
                      month = "未知"; // 或其他默认值
                  }
              } catch (Exception e) {
                  Log.e(TAG, "groupDiariesByMonth: Error parsing date for diary ID: " + diary.getId(), e);
                  // 兜底逻辑，尝试提取月份，如果失败则使用默认值
                  if (diary.getTime() != null && diary.getTime().length() >= 7) {
                      month = diary.getTime().substring(5, 7);
                  } else {
                      month = "未知";
                  }
              }   
              if (!month.equals(lastMonth)) {
                  diaryListItems.add(new DiaryListItem(DiaryListItem.TYPE_MONTH_HEADER, month, null));
                  lastMonth = month;
              }
              diaryListItems.add(new DiaryListItem(DiaryListItem.TYPE_DIARY, null, diary));
          }
          Log.d(TAG, "groupDiariesByMonth: 完成分组，列表项数量=" + diaryListItems.size());
      }   
    //搜索相关
    private void initSearchBar() {
        searchBar = findViewById(R.id.search_bar);
        etSearch = findViewById(R.id.et_search);
        btnSearchClose = findViewById(R.id.btn_search_close);

        btnSearchClose.setOnClickListener(v -> exitSearchMode());

        // 实时搜索：监听文本变化
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 只有输入不为空时才搜索
                String keyword = s.toString().trim();
                if (!keyword.isEmpty()) {
                    performSearch(keyword);
                } else {
                    // 当用户手动清空搜索框时，也恢复原始列表
                    if (isSearching) { // 确保在搜索模式下清空才恢复
                        restoreOriginalList();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
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
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    private void exitSearchMode() {
        isSearching = false;
        searchBar.setVisibility(View.GONE);
        etSearch.setText(""); // 清空文本框
        etSearch.clearFocus();
        // 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
        // 显式恢复原始列表数据
        restoreOriginalList();
    }

    /**
     * 恢复原始的日记列表项并更新 Adapter
     */
    private void restoreOriginalList() {
        if (!originalDiaryListItems.isEmpty()) {
            diaryListItems.clear();
            diaryListItems.addAll(originalDiaryListItems);
            if (adopter != null) {
                adopter.notifyDataSetChanged();
            }
            Log.d(TAG, "Restored original list items.");
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


      /**
       * 处理编辑日记的操作，启动 AddDiary Activity
       * @param diaryToEdit 要编辑的日记对象
       */
      private void EditDiary(Diary diaryToEdit) {
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
          intent.putExtra("mode", 3); // 编辑模式
          startActivityForResult(intent, 1);
          Log.d(TAG, "EditDiary: Starting AddDiary for editing diary ID: " + diaryToEdit.getId());
      }   

    /**
     * 处理删除日记的操作，显示确认对话框
     * @param diaryToDelete 要删除的日记对象
     */
    private void handleDeleteDiaryAction(Diary diaryToDelete) {
        showDeleteConfirmationDialog(diaryToDelete);
        Log.d(TAG, "handleDeleteDiaryAction: Showing delete confirmation for diary ID: " + diaryToDelete.getId());
    }


//    private static final int ID_DIARY_LV = R.id.diary_lv;  // 提取为常量

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
                        EditDiary(diaryToEdit); // 调用提取的方法
                    }

                    @Override
                    public void onDeleteClicked(Diary diaryToDelete) {
                        handleDeleteDiaryAction(diaryToDelete); // 调用新方法
                    }
                }, isTimeReverse(), () -> {
                    // 删除动画结束后的回调
                    refreshListView();
                });
            }
            // 如果是月份头部，什么都不做
        }
    }

    // 显示删除确认对话框的方法
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


    private void initSettingPage() {
        // 只在设置页（container_diary）可见时初始化
        setChangePwd = findViewById(R.id.set_changePwd);
        setAway = findViewById(R.id.set_away);
        LinearLayout setTime = findViewById(R.id.set_time); // 新增
        if (setChangePwd != null) {
            setChangePwd.setOnClickListener(v -> showChangePwdDialog());
        }
        if (setAway != null) {
            setAway.setOnClickListener(v -> logout());
        }
        if (setTime != null) {
            setTime.setOnClickListener(v -> startTimeReverse());
        }
    }

    //根据性别更新
    private void initSexChange(){
        // 动态设置背景
        FrameLayout containerEntries = findViewById(R.id.container_entries);
        FrameLayout containerCalendar = findViewById(R.id.container_calendar);
        if (containerEntries != null) {
            containerEntries.setBackgroundResource(
                    GenderResourceUtil.getMainBackgroundRes(this)
            );
        }
        if (containerCalendar != null) {
            containerCalendar.setBackgroundResource(
                    GenderResourceUtil.getMainBackgroundRes(this)
            );
        }

        // 动态设置底部导航栏颜色
        View bottomBar = findViewById(R.id.bottom_navigation_bar);
        if (bottomBar != null) {
            bottomBar.setBackgroundResource(
                    GenderResourceUtil.getBottomBarColorRes(this)
            );
        }

        // 动态设置select_by_day页面编辑按钮的背景色
        ImageButton btnEditDay = findViewById(R.id.btn_edit_day);
        if (btnEditDay != null) {
            int bgColor = getResources().getColor(GenderResourceUtil.getTabMainColorRes(this));
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(bgColor);
            btnEditDay.setBackground(drawable);
        }

        // 获取性别相关颜色
        int mainColor = getResources().getColor(
                GenderResourceUtil.getTabMainColorRes(this)
        );
        int whiteColor = getResources().getColor(R.color.white);

        // 动态设置设置页面的颜色
        TextView setChangePwdText = findViewById(R.id.set_changePwd_text);
        ImageView setLine1 = findViewById(R.id.set_line1);
        TextView setTimeText = findViewById(R.id.set_time_text);
        ImageView setLine2 = findViewById(R.id.set_line2);
        TextView setAwayText = findViewById(R.id.set_away_text);
        ImageView setLine3 = findViewById(R.id.set_line3);

        if (setChangePwdText != null) {
            setChangePwdText.setTextColor(mainColor);
        }
        if (setLine1 != null) {
            setLine1.setBackgroundColor(mainColor);
        }
        if (setTimeText != null) {
            setTimeText.setTextColor(mainColor);
        }
        if (setLine2 != null) {
            setLine2.setBackgroundColor(mainColor);
        }
        if (setAwayText != null) {
            setAwayText.setTextColor(mainColor);
        }
        if (setLine3 != null) {
            setLine3.setBackgroundColor(mainColor);
        }

        // 1. 动态设置Tab文字颜色
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_selected},    // selected
                new int[]{-android.R.attr.state_selected}     // unselected
        };
        int[] colors = new int[]{
                whiteColor,      // selected
                mainColor        // unselected
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);

        if (tabEntries != null) tabEntries.setTextColor(colorStateList);
        if (tabCalendar != null) tabCalendar.setTextColor(colorStateList);
        if (tabDiary != null) tabDiary.setTextColor(colorStateList);

        // 2. 动态设置Tab背景（底色和边框+圆角）
        setTabBackground(tabEntries, mainColor, whiteColor, "left");
        setTabBackground(tabCalendar, mainColor, whiteColor, "center");
        setTabBackground(tabDiary, mainColor, whiteColor, "right");
    }

    // 辅助方法：为Tab设置selector背景，选中为主色，未选中为白色，边框始终为主色
    private void setTabBackground(TextView tab, int mainColor, int whiteColor, String position) {
        if (tab == null) return;
        float radius = dp2px(tab.getContext(), 8); // 8dp圆角
        float[] leftCorners = new float[]{radius, radius, 0, 0, 0, 0, radius, radius};
        float[] rightCorners = new float[]{0, 0, radius, radius, radius, radius, 0, 0};
        float[] centerCorners = new float[]{0, 0, 0, 0, 0, 0, 0, 0};

        float[] corners;
        switch (position) {
            case "left":
                corners = leftCorners;
                break;
            case "right":
                corners = rightCorners;
                break;
            case "center":
            default:
                corners = centerCorners;
                break;
        }

        // 选中状态
        GradientDrawable selectedDrawable = new GradientDrawable();
        selectedDrawable.setColor(mainColor); // 填充主色
        selectedDrawable.setStroke(2, mainColor); // 边框主色
        selectedDrawable.setCornerRadii(corners);

        // 未选中状态
        GradientDrawable unselectedDrawable = new GradientDrawable();
        unselectedDrawable.setColor(whiteColor); // 填充白色
        unselectedDrawable.setStroke(2, mainColor); // 边框主色
        unselectedDrawable.setCornerRadii(corners);

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, selectedDrawable);
        stateListDrawable.addState(new int[]{-android.R.attr.state_selected}, unselectedDrawable);

        tab.setBackground(stateListDrawable);
    }

    // dp转px工具
    private float dp2px(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    // 修改密码弹窗
    private void showChangePwdDialog() {
        // 获取当前账号
        UserCRUD userCRUD = new UserCRUD(this);
        userCRUD.open();
        Cursor cursor = userCRUD.db.query(UserDatabase.USER_TABLE, new String[]{UserDatabase.EMAIL}, UserDatabase.USER_ID + "=?", new String[]{String.valueOf(currentUserId)}, null, null, null);
        String email = "";
        if (cursor.moveToFirst()) {
            email = cursor.getString(cursor.getColumnIndex(UserDatabase.EMAIL));
        }
        cursor.close();
        userCRUD.close();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView tvEmail = new TextView(this);
        tvEmail.setText("账号：" + email);
        layout.addView(tvEmail);

        final EditText etNewPwd = new EditText(this);
        etNewPwd.setHint("请输入新密码");
        etNewPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPwd);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("修改密码")
                .setView(layout)
                .setPositiveButton("确认", (dialog, which) -> {
                    String newPwd = etNewPwd.getText().toString().trim();
                    if (newPwd.isEmpty()) {
                        ToastUtil.showMsg(this, "新密码不能为空");
                        return;
                    }
                    UserCRUD crud = new UserCRUD(this);
                    crud.open();
                    ContentValues values = new ContentValues();
                    values.put(UserDatabase.PASSWORD, newPwd);
                    crud.db.update(UserDatabase.USER_TABLE, values, UserDatabase.USER_ID + "=?", new String[]{String.valueOf(currentUserId)});
                    crud.close();
                    ToastUtil.showMsg(this, "密码修改成功");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void logout() {
        // 清除自动登录信息
        getSharedPreferences("login", MODE_PRIVATE).edit().clear().apply();
        // 跳转到登录界面并清空输入框
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        if (isTimeReversing) {
            // 彻底禁止返回
            ToastUtil.showMsg(this, "时光倒流中，请稍候...");
            return;
        }
        if (System.currentTimeMillis() - lastBackPressedTime > 2000) {
            ToastUtil.showMsg(this, "再按一次返回退出");
            lastBackPressedTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private boolean isTimeReversing = false;
    private List<Diary> backupDiaries = new ArrayList<>();
    private List<DiaryListItem> backupDiaryListItems = new ArrayList<>();
    private View overlayView; // 用于遮罩


    private void startTimeReverse() {
        if (isTimeReversing) return;
        isTimeReversing = true;
        showOverlay();
        switchPage(0);
        backupDiaries.clear();
        backupDiaries.addAll(diaryList);
        backupDiaryListItems.clear();
        backupDiaryListItems.addAll(diaryListItems);

        // 保存备份到本地文件
        saveBackupDiariesToFile();

        if (diaryList.size() == 0) {
            finishTimeReverse();
            return;
        }
        Diary latestDiary = diaryList.get(0);
        new ImfDiary().showDiaryDialog(this, latestDiary, new ImfDiary.DialogActionListener() {
            @Override
            public void onEditClicked(Diary diaryToEdit) {}
            @Override
            public void onDeleteClicked(Diary diaryToDelete) {}
        }, true, () -> {
            deleteDiaryWithAnimation(latestDiary, () -> {
                deleteAllDiariesWithAnimation();
            });
        });
    }

    // 保存备份到本地文件
    private void saveBackupDiariesToFile() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(backupDiaries);
            java.io.FileOutputStream fos = openFileOutput(BACKUP_FILE, MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOverlay() {
        if (overlayView == null) {
            overlayView = new View(this);
            overlayView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            overlayView.setBackgroundColor(0x00000000); // 透明遮罩
            overlayView.setClickable(true); // 拦截点击
            overlayView.setFocusable(true);
            overlayView.setFocusableInTouchMode(true);
            overlayView.setOnTouchListener((v, event) -> true); // 拦截所有触摸事件
            ((ViewGroup) findViewById(android.R.id.content)).addView(overlayView);//添加到窗口内容的最顶层容器
        }
        overlayView.setVisibility(View.VISIBLE);
    }


    private void hideOverlay() {
        if (overlayView != null) overlayView.setVisibility(View.GONE);
    }

    private void deleteDiaryWithAnimation(Diary diary, Runnable onFinish) {
        // 删除前先刷新列表
        new Handler().postDelayed(() -> {
            CRUD op = new CRUD(this);
            op.open();
            op.deleteDiary(diary);
            op.close();
            refreshListView();
            new Handler().postDelayed(onFinish, 100);//控制删除日记后的等待时间。
        }, 100); //控制删除日记前的等待时间。
    }

    private void deleteAllDiariesWithAnimation() {
        if (diaryList.size() == 0) {
            finishTimeReverse();
            return;
        }
        Diary diary = diaryList.get(0);
        deleteDiaryWithAnimation(diary, this::deleteAllDiariesWithAnimation);//循环调用
    }

    private void finishTimeReverse() {
        hideOverlay(); // <<--- 时光倒流结束，移除遮罩
        Intent intent = new Intent(this, TimeReverseEndActivity.class);
        startActivity(intent);
    }


    private void restoreDiaries() {
        // 优先从本地文件恢复
        List<Diary> restoreList = readBackupDiariesFromFile();
        if (restoreList != null && !restoreList.isEmpty()) {
            backupDiaries.clear();
            backupDiaries.addAll(restoreList);
        }
        // 先清空当前用户所有日记
        CRUD op = new CRUD(this);
        op.open();
        List<Long> userDiaryIds = new ArrayList<>();
        UserCRUD userCRUD = new UserCRUD(this);
        userCRUD.open();
        Cursor cursor = userCRUD.getUserDiaryIds(currentUserId);
        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndex(UserDatabase.DIARY_ID);
            if (columnIndex != -1) {
                userDiaryIds.add(cursor.getLong(columnIndex));
            }
        }
        cursor.close();
        userCRUD.close();
        for (Long id : userDiaryIds) {
            op.deleteDiaryById(id);
        }
        // 恢复所有备份日记
        for (Diary d : backupDiaries) {
            Diary newDiary = op.addDiary(d);
            // 重新建立用户-日记关联
            UserCRUD crud = new UserCRUD(this);
            crud.open();
            crud.linkUserDiary(currentUserId, newDiary.getId());
            crud.close();
        }
        op.close();
        refreshListView();
        ToastUtil.showMsg(this, "已恢复全部日记");
        // 恢复后删除本地备份文件
        deleteBackupFile();
    }
    // 从本地文件读取备份
    private List<Diary> readBackupDiariesFromFile() {
        try {
            java.io.FileInputStream fis = openFileInput(BACKUP_FILE);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            fis.close();
            String json = baos.toString();
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<List<Diary>>(){}.getType());
        } catch (Exception e) {
            // 文件不存在或解析失败
            return null;
        }
    }

    // 删除备份文件
    private void deleteBackupFile() {
        java.io.File file = new java.io.File(getFilesDir(), BACKUP_FILE);
        if (file.exists()) file.delete();
    }

    // 检查备份文件是否存在
    private boolean hasBackupFile() {
        java.io.File file = new java.io.File(getFilesDir(), BACKUP_FILE);
        return file.exists();
    }


    private void setupCalendarDecorators() {
        calendarView = findViewById(R.id.calendarView);
        if (calendarView == null) {
            Log.e(TAG, "setupCalendarDecorators: calendarView 未找到!");
            return;
        }

        // 仅在第一次初始化时设置选中当天
        if (!isCalendarInitialized) {//还未初始化
            calendarView.setSelectedDate(CalendarDay.today());
            Log.d(TAG, "setupCalendarDecorators: 首次初始化，已设置日历选中当天: " + CalendarDay.today().toString());
            isCalendarInitialized = true; // 设置标志位为 true 
        }
        Log.d(TAG, "setupCalendarDecorators: 开始设置日历装饰器");
        calendarView.removeDecorators(); // 清除旧装饰器

        // HashSet 自动处理重复日期，一个日期只会被添加一次
        Set<CalendarDay> blueDotDays = new HashSet<>();
        Set<CalendarDay> yellowDotDays = new HashSet<>(); 

        // diaryList ：包含所有日记数据的列表
        if (diaryList == null) {
            Log.w(TAG, "setupCalendarDecorators: diaryList 为 null，无法设置装饰器");
            diaryList = new ArrayList<>(); // 初始化以避免空指针
        }

        Log.d(TAG, "setupCalendarDecorators: 当前日记数量: " + diaryList.size());

        // 遍历所有日记，收集日期
        for (Diary diary : diaryList) {
            String time = diary.getTime();
            if (time != null && time.length() >= 10) {
                try {
                    String[] dateParts = time.split(" ")[0].split("-");
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]); 
                    int day = Integer.parseInt(dateParts[2]);
                    // CalendarDay 月份是从 0 开始的 (0-11)
                    CalendarDay calendarDay = CalendarDay.from(year, month - 1, day);
                    if (diary.getTag() == 2) { // 黄色标记
                        yellowDotDays.add(calendarDay); // 添加到黄点集合
                        Log.d(TAG, "setupCalendarDecorators: 添加黄点日期: " + calendarDay.toString());
                    } else { // 蓝点标记 (假设 tag!=2 都用蓝点)
                        blueDotDays.add(calendarDay);
                        // Log.d(TAG, "setupCalendarDecorators: 添加蓝点日期: " + calendarDay.toString());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "setupCalendarDecorators: 解析日期时间出错: " + time, e);
                }
            }
        }

        // 只想显示一个点（黄点优先）
        blueDotDays.removeAll(yellowDotDays);

        Log.d(TAG, "setupCalendarDecorators: 最终黄点日期数量: " + yellowDotDays.size());
        Log.d(TAG, "setupCalendarDecorators: 最终蓝点日期数量: " + blueDotDays.size());

        // 添加装饰器
        if (!yellowDotDays.isEmpty()) {
            // YellowDotDecorator 现在画黄点
            calendarView.addDecorator(new YellowDotDecorator(this, yellowDotDays));
            Log.d(TAG, "setupCalendarDecorators: 已添加 YellowDotDecorator");
        }
        if (!blueDotDays.isEmpty()) {
            calendarView.addDecorator(new BlueDotDecorator(this, blueDotDays));
            Log.d(TAG, "setupCalendarDecorators: 已添加 BlueDotDecorator");
        }

        // 强制视图重绘
        calendarView.invalidate();
        Log.d(TAG, "setupCalendarDecorators: 已调用 calendarView.invalidate()");
    }

    // 设置 btn_edit_day 的点击逻辑
    private void setupEditDayButton() {
        ImageButton btnEditDay = findViewById(R.id.btn_edit_day);
        if (btnEditDay == null || calendarView == null) return;

        btnEditDay.setOnClickListener(v -> {
            CalendarDay selectedDay = calendarView.getSelectedDate();
            if (selectedDay == null) return;

            // 获取选中日期字符串 yyyy-MM-dd
            String selectedDateStr = String.format("%04d-%02d-%02d",
                    selectedDay.getYear(), selectedDay.getMonth() + 1, selectedDay.getDay());

            // 获取今天日期字符串
            String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

            // 查询所有日记
            CRUD op = new CRUD(this);
            op.open();
            List<Diary> allDiaries = op.getAllDiary();
            op.close();

            // 只查找属于当前用户的日记
            UserCRUD userCRUD = new UserCRUD(this);
            userCRUD.open();
            java.util.List<Long> userDiaryIds = new java.util.ArrayList<>();
            Cursor cursor = userCRUD.getUserDiaryIds(currentUserId);
            while (cursor.moveToNext()) {
                int columnIndex = cursor.getColumnIndex(UserDatabase.DIARY_ID);
                if (columnIndex != -1) {
                    userDiaryIds.add(cursor.getLong(columnIndex));
                }
            }
            cursor.close();
            userCRUD.close();

            Diary targetDiary = null;
            for (Diary diary : allDiaries) {
                if (userDiaryIds.contains(diary.getId())) {
                    String diaryDate = diary.getTime();
                    if (diaryDate != null && diaryDate.length() >= 10 && diaryDate.substring(0, 10).equals(selectedDateStr)) {
                        targetDiary = diary;
                        break;
                    }
                }
            }

            if (selectedDateStr.equals(todayStr)) {
                // 今天：和主页面一致
                if (targetDiary != null) {
                    // 已有今日日记，进入编辑页面
                    Intent editIntent = new Intent(MainActivity.this, AddDiary.class);
                    editIntent.putExtra("id", targetDiary.getId());
                    editIntent.putExtra("time", targetDiary.getTime());
                    editIntent.putExtra("weather", targetDiary.getWeather());
                    editIntent.putExtra("temperature", targetDiary.getTemperature());
                    editIntent.putExtra("location", targetDiary.getLocation());
                    editIntent.putExtra("title", targetDiary.getTitle());
                    editIntent.putExtra("body", targetDiary.getBody());
                    editIntent.putExtra("mood", targetDiary.getMood());
                    editIntent.putExtra("tag", targetDiary.getTag());
                    editIntent.putExtra("mode", 3); // 编辑模式
                    startActivityForResult(editIntent, 1);
                } else {
                    // 没有今日日记，进入新建页面
                    Intent addIntent = new Intent(MainActivity.this, AddDiary.class);
                    addIntent.putExtra("mode", 4); // 新建模式
                    startActivityForResult(addIntent, 0);
                }
            } else {
                // 非今天
                if (targetDiary != null) {
                    // 2.1 有日记，编辑模式，时间不变
                    Intent editIntent = new Intent(MainActivity.this, AddDiary.class);
                    editIntent.putExtra("id", targetDiary.getId());
                    editIntent.putExtra("time", targetDiary.getTime());
                    editIntent.putExtra("weather", targetDiary.getWeather());
                    editIntent.putExtra("temperature", targetDiary.getTemperature());
                    editIntent.putExtra("location", targetDiary.getLocation());
                    editIntent.putExtra("title", targetDiary.getTitle());
                    editIntent.putExtra("body", targetDiary.getBody());
                    editIntent.putExtra("mood", targetDiary.getMood());
                    editIntent.putExtra("tag", targetDiary.getTag());
                    editIntent.putExtra("mode", 3); // 编辑模式
                    startActivityForResult(editIntent, 1);
                } else {
                    // 2.2 无日记，新增模式，保存时以选中日期00:00
                    Intent addIntent = new Intent(MainActivity.this, AddDiary.class);
                    addIntent.putExtra("mode", 5); // 自定义mode，表示“指定日期新增”
                    addIntent.putExtra("selected_date", selectedDateStr); // 传递选中日期
                    startActivityForResult(addIntent, 2);
                }
            }
        });
    }

}
