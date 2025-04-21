package com.code.mydiary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
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

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import com.amap.api.location.AMapLocationClient;

public class AddDiary extends AppCompatActivity {

    EditText DiaryTitle;
    EditText DiaryBody;
    ImageButton weatherBtn, moodBtn;
    int weather = -1; // -1 表示未选择
    int mood = -1;

    

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
        weatherBtn = findViewById(R.id.imgBt_weather);
        moodBtn = findViewById(R.id.imgBt_mood);

        // 初始为淡灰色
        weatherBtn.setColorFilter(getResources().getColor(R.color.gray_light));
        moodBtn.setColorFilter(getResources().getColor(R.color.gray_light));

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
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putExtra("addDiary_title", DiaryTitle.getText().toString());
            intent.putExtra("addDiary_body", DiaryBody.getText().toString());
            intent.putExtra("addDiary_time", dataToStr());
            intent.putExtra("addDiary_weather", weather);
            intent.putExtra("addDiary_mood", mood);
            intent.putExtra("addDiary_location", lastLocation); // 新增
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
        android.location.LocationManager locationManager = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d("AddDiary", "No location permission");
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
                return;
            }
            android.location.Location location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            Log.d("AddDiary", "GPS_PROVIDER location: " + location);
            if (location == null) {
                location = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
                Log.d("AddDiary", "NETWORK_PROVIDER location: " + location);
            }
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
    
                // 使用高德逆地理编码
                GeocodeSearch geocodeSearch = new GeocodeSearch(this);
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
                        String locationStr = "位置: " + addressStr + " (" + latitude + ", " + longitude + ")";
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
                Log.d("AddDiary", "location is null, cannot get location");
                android.widget.Toast.makeText(this, "无法获取当前位置", android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AddDiary", "Exception in insertLocation", e);
            android.widget.Toast.makeText(this, "获取位置失败", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
