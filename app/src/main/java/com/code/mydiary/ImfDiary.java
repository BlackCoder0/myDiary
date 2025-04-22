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
    public void showDiaryDialog(Context context, final Diary diary, final DialogActionListener listener) {
        if (diary == null) {
            Log.e("ImfDiary", "Diary object is null, cannot show dialog.");
            Toast.makeText(context, "无法加载日记信息", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 去掉标题栏

        View view = LayoutInflater.from(context).inflate(R.layout.imf_diary, null);
        dialog.setContentView(view);

        // --- 获取视图控件 ---
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
        ImageButton btnMore = view.findViewById(R.id.imf_imgBtn_more); // 编辑按钮
        ImageButton btnDelete = view.findViewById(R.id.imf_imgBtn_delte); // 删除按钮

        // --- 填充数据 ---

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
            // 可以设置默认值或错误提示
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
                ivWeather.setVisibility(View.GONE); // 或者设置一个默认图标
            }
            if (!TextUtils.isEmpty(weatherText)) {
                tvWeatherText.setText(weatherText);
                tvWeatherText.setVisibility(View.VISIBLE);
            } else {
                tvWeatherText.setVisibility(View.GONE);
            }
        } else {
            // 天气未设置，隐藏图标和文字
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
            // 位置未设置，隐藏图标和文字
            tvLocationText.setVisibility(View.GONE);
            ivLocation.setVisibility(View.GONE);
        }

        // 4. 处理正文内容
        tvContent.setText(!TextUtils.isEmpty(diary.getBody()) ? diary.getBody() : "无内容");

        // --- 设置按钮监听器 ---

        // 关闭按钮
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // 更多/编辑按钮
        btnMore.setOnClickListener(v -> {
            dialog.dismiss(); // 关闭预览对话框
            if (listener != null) {
                listener.onEditClicked(diary); // 通知 MainActivity 去启动编辑页面
            }
        });

        // 删除按钮
        btnDelete.setOnClickListener(v -> {
            dialog.dismiss(); // 关闭预览对话框
            if (listener != null) {
                listener.onDeleteClicked(diary); // 通知 MainActivity 去处理删除逻辑
            }
        });


        // --- 显示对话框 ---
        Window window = dialog.getWindow();
        if (window != null) {
            // --- 添加这行代码 ---
            window.setBackgroundDrawableResource(android.R.color.transparent); // 设置窗口背景为透明
            // --- 添加结束 ---

            // 设置对话框大小 (可以根据需要调整)
            window.setLayout(
                    (int)(context.getResources().getDisplayMetrics().widthPixels * 0.9),
                    (int)(context.getResources().getDisplayMetrics().heightPixels * 0.7)
            );
             // 可选：移除背景，如果你在 XML 中设置了背景
             // window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCanceledOnTouchOutside(true); // 点击外部也可关闭
        dialog.show();
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