package com.code.mydiary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import com.amap.api.location.AMapLocationClient;

public class AddDiary extends AppCompatActivity implements com.amap.api.location.AMapLocationListener {

    EditText DiaryTitle;
    EditText DiaryBody;
    ImageButton weatherBtn, moodBtn;

    Button btnCancel,btnSave;
    int weather = -1; // -1 表示未选择
    int mood = -1;
    int tag = 1; // 默认 tag 为 1
    String temperature ="25";
    String location="";

    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;


    //String time, int weather, String temperature, String location, String title, String body, int mood, int tag,int mode
    private String old_time = "";
    private int old_weather = -1;
    private String old_temperature = "";
    private String old_location = "";
    private String old_title = "";
    private String old_body = "";
    private int old_mood = -1;
    private int old_tag = 1;
    private long id = 0;
    private int openMode = 0;
//    private int tag = 1;

    public Intent intent = new Intent();//发送
    private boolean tagChange = false;

    

    private static final int[] weatherIcons = {
            R.drawable.ic_weather_cloud,
            R.drawable.ic_weather_foggy,
            R.drawable.ic_weather_rainy,
            R.drawable.ic_weather_snowy,
            R.drawable.ic_weather_sunny,
            R.drawable.ic_weather_windy
    };

    private static final int[] moodIcons = {
            R.drawable.ic_mood_happy,
            R.drawable.ic_mood_soso,
            R.drawable.ic_mood_unhappy
    };

    int weatherIndex = 0;
    int moodIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);
        setContentView(R.layout.add_diary_activity);
        DiaryTitle = findViewById(R.id.edit_adddiary_title);
        DiaryBody = findViewById(R.id.edit_adddiary_body);
        // 新增：获取按钮实例
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);

        Intent receivedIntent = getIntent();
        openMode = receivedIntent.getIntExtra("mode", 4);

        if(openMode == 3){//打开已存在的diary
            id = receivedIntent.getLongExtra("id",0);
            old_time = receivedIntent.getStringExtra("time");
            old_weather = receivedIntent.getIntExtra("weather",-1);
            old_temperature = receivedIntent.getStringExtra("temperature");
            old_location = receivedIntent.getStringExtra("location");
            old_title = receivedIntent.getStringExtra("title");
            old_body = receivedIntent.getStringExtra("body");
            old_mood = receivedIntent.getIntExtra("mood",-1);
            old_tag = receivedIntent.getIntExtra("tag",1); // 获取旧的 tag

            DiaryTitle.setText(old_title);
            DiaryBody.setText(old_body);
            DiaryBody.setSelection(old_body.length());

            // 初始化当前状态为旧状态
            weather = old_weather;
            mood = old_mood;
            tag = old_tag; // 使用旧的 tag 初始化当前 tag
            temperature = old_temperature;
            location = old_location;
            lastLocation = old_location;

        } else {
            // 新建模式
            old_title = "";
            old_body = "";
            // 新建时，确保 old_time 为空或默认值，以便后续判断是否需要更新时间
            old_time = ""; // 或者设置为一个不可能的默认值
        }

        weatherBtn = findViewById(R.id.imgBt_weather);
        moodBtn = findViewById(R.id.imgBt_mood);

        // 根据当前 weather 和 mood 设置按钮状态 (包括编辑模式加载旧值)
        if (weather != -1 && weather < weatherIcons.length) {
            weatherBtn.setImageResource(weatherIcons[weather]);
            weatherBtn.setColorFilter(getResources().getColor(R.color.boy));
        } else {
            // 如果是新建或者旧数据无效，则显示默认灰色
            weatherBtn.setImageResource(R.drawable.ic_weather_sunny); // 可以设置一个默认图标
            weatherBtn.setColorFilter(getResources().getColor(R.color.gray_light));
        }
        if (mood != -1 && mood < moodIcons.length) {
            moodBtn.setImageResource(moodIcons[mood]);
            moodBtn.setColorFilter(getResources().getColor(R.color.boy));
        } else {
            // 如果是新建或者旧数据无效，则显示默认灰色
            moodBtn.setImageResource(R.drawable.ic_mood_happy); // 可以设置一个默认图标
            moodBtn.setColorFilter(getResources().getColor(R.color.gray_light));
        }

        weatherBtn.setOnClickListener(v -> showWeatherDialog());
        moodBtn.setOnClickListener(v -> showMoodDialog());

        // 新增：插入时间按钮
        ImageButton btnInsertTime = findViewById(R.id.btn_insert_time);
        btnInsertTime.setOnClickListener(v -> {
            String currentTime = new SimpleDateFormat("\n[HH:mm:ss]\n", Locale.getDefault()).format(new Date());
            int cursorPos = DiaryBody.getSelectionStart();
            String oldText = DiaryBody.getText().toString();
            String newText = oldText.substring(0, cursorPos) + currentTime + oldText.substring(cursorPos);
            DiaryBody.setText(newText);
            DiaryBody.setSelection(cursorPos + currentTime.length());
        });

        ImageButton btnInsertLocation = findViewById(R.id.btn_insert_location);
        btnInsertLocation.setOnClickListener(v -> {
            Log.d("AddDiary", "btnInsertLocation clicked");
            insertLocation();
        });
        // 新增：为取消和保存按钮设置监听器
        btnCancel.setOnClickListener(v -> performCancel());
        btnSave.setOnClickListener(v -> performSave());

        Log.d("AddDiary", "onCreate is called");

//        weatherBtn.setOnClickListener(v -> {
//            weatherIndex = (weatherIndex + 1) % weatherIcons.length;
//            weatherBtn.setImageResource(weatherIcons[weatherIndex]);
//            weather = weatherIndex;
//            Log.d("Weather", "Weather selected index: " + weather);
//        });
//
//        moodBtn.setOnClickListener(v -> {
//            moodIndex = (moodIndex + 1) % moodIcons.length;
//            moodBtn.setImageResource(moodIcons[moodIndex]);
//            mood = moodIndex;
//            Log.d("Mood", "Mood selected index: " + mood);
//        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        // --- 修改开始: 返回键行为修改为显示确认对话框 ---
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitConfirmationDialog(); // 调用显示确认对话框的方法
            return true; // 事件已处理
        // --- 修改结束 ---
        }
        return super.onKeyDown(keyCode, event);
    }

    // 新增：执行保存操作的方法
    private void performSave() {
        Intent resultIntent = new Intent();
        int currentMode = autoSetMessageMode();

        resultIntent.putExtra("mode", currentMode);

        // 只有在新建(0)或修改(1)模式下才需要传递详细数据
        if (currentMode == 0 || currentMode == 1) {
            resultIntent.putExtra("title", DiaryTitle.getText().toString());
            resultIntent.putExtra("body", DiaryBody.getText().toString());
            resultIntent.putExtra("weather", weather);
            resultIntent.putExtra("mood", mood);
            resultIntent.putExtra("tag", tag);
            resultIntent.putExtra("temperature", temperature);
            // 使用 lastLocation 如果它有效，否则回退到 old_location (主要用于编辑模式下未重新获取位置的情况)
            String finalLocation = (lastLocation != null && !lastLocation.isEmpty()) ? lastLocation : old_location;
            // 新增：去除换行和中括号
            String cleanLocation = finalLocation.replace("\n", "").replace("[", "").replace("]", "");
            resultIntent.putExtra("location", cleanLocation);

            if (currentMode == 1) { // 编辑模式
                resultIntent.putExtra("id", id);
                resultIntent.putExtra("time", old_time); // 编辑模式使用旧时间
            } else { // 新建模式 (currentMode == 0)
                resultIntent.putExtra("time", dataToStr()); // 新建模式使用当前时间
            }
        }
        // 即使 mode 是 -1 (编辑模式无更改)，也返回 RESULT_OK，让 MainActivity 判断 mode
        setResult(RESULT_OK, resultIntent);
        finish(); // 关闭当前 Activity
    }

    // 新增：执行取消操作的方法
    private void performCancel() {
        // 设置结果为 CANCELED，表示用户取消了操作
        setResult(RESULT_CANCELED);
        finish(); // 关闭当前 Activity
    }


    // 建议在 autoSetMessageMode 中也加入 tag 的比较，虽然当前 tag 不能在 AddDiary 修改
    public int autoSetMessageMode(){
        String currentTitle = DiaryTitle.getText().toString();
        String currentBody = DiaryBody.getText().toString();

        if(openMode == 4){//新建模式
            // 检查是否有输入内容，或者是否选择了天气/心情
            if(currentBody.isEmpty() && currentTitle.isEmpty() && weather == -1 && mood == -1){
                return -1; // 认为是无意义的输入
            }
            else{
                return 0; // 认为是新建
            }
        }
        else { // openMode == 3 (编辑模式)
            // 检查是否有任何更改
            boolean changed = !currentTitle.equals(old_title) ||
                              !currentBody.equals(old_body) ||
                              weather != old_weather ||
                              mood != old_mood ||
                              tag != old_tag ||
                              !temperature.equals(old_temperature) ||
                              // 比较最终位置和旧位置
                              !((lastLocation != null && !lastLocation.isEmpty()) ? lastLocation : old_location).equals(old_location);

            if (!changed) {
                return -1; // 无更改
            } else {
                return 1; // 有更改
            }
        }
    }

    public String dataToStr(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
    private String getCurrentTime() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(date);
    }

    private void showWeatherDialog() {
        showIconDialog(
            weatherBtn,
            new int[]{
                R.drawable.ic_weather_cloud,
                R.drawable.ic_weather_foggy,
                R.drawable.ic_weather_rainy,
                R.drawable.ic_weather_snowy,
                R.drawable.ic_weather_sunny,
                R.drawable.ic_weather_windy
            },
            true
        );
    }

    private void showMoodDialog() {
        showIconDialog(
            moodBtn,
            new int[]{
                R.drawable.ic_mood_happy,
                R.drawable.ic_mood_soso,
                R.drawable.ic_mood_unhappy
            },
            false
        );
    }

    private void showIconDialog(ImageButton targetBtn, int[] icons, boolean isWeather) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_grid_icons, null);
        GridView gridView = dialogView.findViewById(R.id.grid_icons);
        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() { return icons.length; }
            @Override
            public Object getItem(int i) { return icons[i]; }
            @Override
            public long getItemId(int i) { return i; }
            @Override
            public View getView(int i, View convertView, ViewGroup parent) {
                ImageView iv = new ImageView(AddDiary.this);
                iv.setImageResource(icons[i]);
                iv.setPadding(24,24,24,24);
                iv.setLayoutParams(new GridView.LayoutParams(150, 150));
                return iv;
            }
        });
        gridView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            targetBtn.setImageResource(icons[position]);
            targetBtn.setColorFilter(getResources().getColor(R.color.boy));
            if (isWeather) {
                weather = position;
            } else {
                mood = position;
            }
            dialog.dismiss();
        });
        // 只设置点击外部可关闭，不做任何选择
        dialog.setContentView(dialogView);
        dialog.setCanceledOnTouchOutside(true); // 点击外部关闭
        dialog.show();
    }

    private String lastLocation = ""; // 用于保存最后一次获取的位置

    //获取位置和高德SDK逆编码
    private void insertLocation() {
        Log.d("AddDiary", "insertLocation called");
        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d("AddDiary", "No location permission");
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
                return;
            }
            // 初始化定位
            if (mlocationClient == null) {
                mlocationClient = new AMapLocationClient(getApplicationContext());
                mLocationOption = new AMapLocationClientOption();
                mlocationClient.setLocationListener(this);
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                mLocationOption.setInterval(2000); // 可根据需要调整
                mlocationClient.setLocationOption(mLocationOption);
            }
            mlocationClient.startLocation(); // 启动定位
        } catch (Exception e) {
            Log.e("AddDiary", "Exception in insertLocation", e);
            android.widget.Toast.makeText(this, "获取位置失败: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    // 新增：显示退出确认对话框的方法
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认退出") // 设置对话框标题
                .setMessage("您确定要退出编辑吗？未保存的更改将会丢失。") // 设置对话框消息
                .setPositiveButton("确认退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击确认退出，调用取消操作
                        performCancel();
                    }
                })
                .setNegativeButton("取消", null) // 用户点击取消，对话框消失，不做任何操作
                .show(); // 显示对话框
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mlocationClient != null) {
            mlocationClient.onDestroy();
        }
    }

    @Override
    public void onLocationChanged(com.amap.api.location.AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                double latitude = amapLocation.getLatitude();
                double longitude = amapLocation.getLongitude();
                // 定位成功后停止定位
                if (mlocationClient != null) {
                    mlocationClient.stopLocation();
                }
                // 使用高德逆地理编码
                GeocodeSearch geocodeSearch = null;
                try {
                    geocodeSearch = new GeocodeSearch(this);
                } catch (AMapException e) {
                    throw new RuntimeException(e);
                }
                LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);

                geocodeSearch.getFromLocationAsyn(query);
                geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                    @Override
                    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
                        String addressStr;
                        if (rCode == 1000 && regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null) {
                            RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
                            addressStr = address.getFormatAddress();
                        } else {
                            addressStr = "高德地址解析失败";
                        }
                        String locationStr = "\n[" + addressStr + "]\n";
                        Log.d("LocationTest", "location=" + locationStr);
                        lastLocation = locationStr;
                        // 插入到正文
                        int cursorPos = DiaryBody.getSelectionStart();
                        String oldText = DiaryBody.getText().toString();
                        String newText = oldText.substring(0, cursorPos) + locationStr + oldText.substring(cursorPos);
                        DiaryBody.setText(newText);
                        DiaryBody.setSelection(cursorPos + locationStr.length());
                    }

                    @Override
                    public void onGeocodeSearched(com.amap.api.services.geocoder.GeocodeResult geocodeResult, int i) {
                        // 不需要实现
                    }
                });
            } else {
                Log.e("AmapError", "location Error, ErrCode:" + amapLocation.getErrorCode() + ", errInfo:" + amapLocation.getErrorInfo());
                android.widget.Toast.makeText(this, "定位失败: " + amapLocation.getErrorInfo(), android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
}
