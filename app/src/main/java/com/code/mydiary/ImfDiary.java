package com.code.mydiary;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;

import com.code.mydiary.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ImfDiary {

    // --- 新增接口 ---
    // 用于通知 MainActivity 执行操作 (编辑或删除)
    public interface DialogActionListener {
        void onEditClicked(Diary diaryToEdit);
        void onDeleteClicked(Diary diaryToDelete);
    }
    // --- 新增接口结束 ---


    // 修改方法签名，接收 Diary 对象和监听器
// ... 其他 import ...
 // 新增 import

// ... 其他代码 ...

    // 新增重载方法
    public void showDiaryDialog(Context context, final Diary diary, final DialogActionListener listener, boolean isTimeReverse, Runnable onDeleteAnimFinish) {
        if (diary == null) {
            Log.e("ImfDiary", "Diary object is null, cannot show dialog.");
            Toast.makeText(context, "无法加载日记信息", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.imf_diary, null);
        dialog.setContentView(view);

        // 获取视图控件
        ImageButton btnClose = view.findViewById(R.id.imf_imgBtn_close);
        TextView tvMoon = view.findViewById(R.id.imf_tv_moon);
        TextView tvDay = view.findViewById(R.id.imf_tv_day);
        TextView tvWeek = view.findViewById(R.id.imf_tv_week);
        TextView tvTime = view.findViewById(R.id.imf_tv_time);
        ImageView ivWeather = view.findViewById(R.id.iv_weather_image);
        TextView tvWeatherText = view.findViewById(R.id.imf_tv_weather_text);
        ImageView ivLocation = view.findViewById(R.id.iv_location_image);
        TextView tvLocationText = view.findViewById(R.id.imf_tv_location_text);
        TextView tvContent = view.findViewById(R.id.imf_tv_content);
        ImageButton btnMore = view.findViewById(R.id.imf_imgBtn_more);
        ImageButton btnDelete = view.findViewById(R.id.imf_imgBtn_delte);

        // 填充数据
        // 1. 处理时间
        String timeStr = diary.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = sdf.parse(timeStr);
            if (date != null) {
                tvMoon.setText(new SimpleDateFormat("MMMM", Locale.ENGLISH).format(date)); // 月份英文全称
                tvDay.setText(new SimpleDateFormat("d", Locale.getDefault()).format(date)); // 日
                tvWeek.setText(new SimpleDateFormat("EEEE.", Locale.ENGLISH).format(date)); // 星期英文全称
                tvTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)); // 时:分
            }
        } catch (ParseException e) {
            Log.e("ImfDiary", "Error parsing date: " + timeStr, e);
            tvMoon.setText("Error");
            tvDay.setText("");
            tvWeek.setText("");
            tvTime.setText("");
        }

        // 2. 处理天气
        int weatherIndex = diary.getWeather();
        if (weatherIndex != -1) {
            int weatherIconResId = getWeatherIconResId(weatherIndex);
            String weatherText = getWeatherText(weatherIndex);
            if (weatherIconResId != 0) {
                ivWeather.setImageResource(weatherIconResId);
                ivWeather.setVisibility(View.VISIBLE);
            } else {
                ivWeather.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(weatherText)) {
                tvWeatherText.setText(weatherText);
                tvWeatherText.setVisibility(View.VISIBLE);
            } else {
                tvWeatherText.setVisibility(View.GONE);
            }
        } else {
            ivWeather.setVisibility(View.GONE);
            tvWeatherText.setVisibility(View.GONE);
        }

        // 3. 处理位置
        String location = diary.getLocation();
        if (!TextUtils.isEmpty(location)) {
            tvLocationText.setText(location);
            tvLocationText.setVisibility(View.VISIBLE);
            ivLocation.setVisibility(View.VISIBLE);
        } else {
            tvLocationText.setVisibility(View.GONE);
            ivLocation.setVisibility(View.GONE);
        }

        // 4. 处理正文内容
        tvContent.setText(!TextUtils.isEmpty(diary.getBody()) ? diary.getBody() : "无内容");

        // 设置按钮监听器
        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnMore.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onEditClicked(diary);
            }
        });
        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onDeleteClicked(diary);
            }
        });

        // 如果是时光倒流模式，执行动画
        if (isTimeReverse) {
            btnClose.setEnabled(false);
            btnMore.setEnabled(false);
            btnDelete.setEnabled(false);
            startDeleteAnim(tvMoon, tvDay, tvWeek, tvTime, tvWeatherText, tvLocationText, tvContent, onDeleteAnimFinish, dialog);
        }

        // 显示对话框
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(
                    (int)(context.getResources().getDisplayMetrics().widthPixels * 0.9),
                    (int)(context.getResources().getDisplayMetrics().heightPixels * 0.7)
            );
        }
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    // 新增：动画逐步乱码并删除
    private void startDeleteAnim(TextView tvMoon, TextView tvDay, TextView tvWeek, TextView tvTime, TextView tvWeatherText, TextView tvLocationText, TextView tvContent, Runnable onFinish, Dialog dialog) {
        Handler handler = new Handler();
        Runnable[] steps = new Runnable[2];
        steps[0] = () -> {
            tvMoon.setText(randomGarble(tvMoon.getText().toString()));
            tvDay.setText(randomGarble(tvDay.getText().toString()));
            tvWeek.setText(randomGarble(tvWeek.getText().toString()));
            tvTime.setText(randomGarble(tvTime.getText().toString()));
            tvWeatherText.setText(randomGarble(tvWeatherText.getText().toString()));
            tvLocationText.setText(randomGarble(tvLocationText.getText().toString()));
            tvContent.setText(randomGarble(tvContent.getText().toString()));
            handler.postDelayed(steps[1], 400);
        };
        steps[1] = () -> {
            deleteTextAnim(tvMoon, tvDay, tvWeek, tvTime, tvWeatherText, tvLocationText, tvContent, onFinish, dialog);
        };
        handler.post(steps[0]);
    }

    // 新增：逐字删除动画
    private void deleteTextAnim(TextView tvMoon, TextView tvDay, TextView tvWeek, TextView tvTime, TextView tvWeatherText, TextView tvLocationText, TextView tvContent, Runnable onFinish, Dialog dialog) {
        if (tvMoon.getText().length() > 0 || tvDay.getText().length() > 0 || tvWeek.getText().length() > 0 || tvTime.getText().length() > 0 || tvWeatherText.getText().length() > 0 || tvLocationText.getText().length() > 0 || tvContent.getText().length() > 0) {
            if (tvMoon.getText().length() > 0) tvMoon.setText(tvMoon.getText().subSequence(0, tvMoon.getText().length() - 1));
            if (tvDay.getText().length() > 0) tvDay.setText(tvDay.getText().subSequence(0, tvDay.getText().length() - 1));
            if (tvWeek.getText().length() > 0) tvWeek.setText(tvWeek.getText().subSequence(0, tvWeek.getText().length() - 1));
            if (tvTime.getText().length() > 0) tvTime.setText(tvTime.getText().subSequence(0, tvTime.getText().length() - 1));
            if (tvWeatherText.getText().length() > 0) tvWeatherText.setText(tvWeatherText.getText().subSequence(0, tvWeatherText.getText().length() - 1));
            if (tvLocationText.getText().length()> 0) tvLocationText.setText(tvLocationText.getText().subSequence(0, tvLocationText.getText().length() - 1));
            if (tvContent.getText().length() > 0) tvContent.setText(tvContent.getText().subSequence(0, tvContent.getText().length() - 1));
            tvContent.postDelayed(() -> deleteTextAnim(tvMoon, tvDay, tvWeek, tvTime, tvWeatherText, tvLocationText, tvContent, onFinish, dialog), 30);
        } else {
            dialog.dismiss();
            if (onFinish != null) onFinish.run();
        }
    }
    // 新增：生成随机乱码
    private String randomGarble(String s) {
        StringBuilder sb = new StringBuilder();
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < s.length(); i++) {
            sb.append((char) (0x4e00 + r.nextInt(0x9fa5 - 0x4e00)));
        }
        return sb.toString();
    }
    // --- Helper 方法 ---

    // (从 DiaryAdopter 移动或复制过来)
    private int getWeatherIconResId(int weather) {
        switch (weather) {
            case 0: return R.drawable.ic_weather_cloud;
            case 1: return R.drawable.ic_weather_foggy;
            case 2: return R.drawable.ic_weather_rainy;
            case 3: return R.drawable.ic_weather_snowy;
            case 4: return R.drawable.ic_weather_sunny;
            case 5: return R.drawable.ic_weather_windy;
            default: return 0; // 返回0表示没有对应图标
        }
    }
    // 新增：根据天气索引获取天气文字描述
    private String getWeatherText(int weather) {
        switch (weather) {
            case 0: return "Cloudy";
            case 1: return "Foggy";
            case 2: return "Rainy";
            case 3: return "Snowy";
            case 4: return "Sunny";
            case 5: return "Windy";
            default: return ""; // 返回空字符串表示没有对应文字
        }
    }




}