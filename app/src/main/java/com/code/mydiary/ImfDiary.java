package com.code.mydiary;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.code.mydiary.R;
import com.code.mydiary.util.GenderResourceUtil;
import com.code.mydiary.util.ToastUtil;


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
    // 新增重载方法
    public void showDiaryDialog(Context context, final Diary diary, final DialogActionListener listener, boolean isTimeReverse, Runnable onDeleteAnimFinish) {
        if (diary == null) {
            Log.e("ImfDiary", "Diary object is null, cannot show dialog.");
            ToastUtil.showMsg(context, "无法加载日记信息");
            return;
        }

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.imf_diary, null);
        dialog.setContentView(view);

        // 获取控件
        ScrollView scrollView = view.findViewById(R.id.imf_scrollView); // 你需要给ScrollView加id: scrollView
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

        // 获取顶部和底部布局
        ConstraintLayout topLayout = view.findViewById(R.id.topLayout);
        ConstraintLayout bottomLayout = view.findViewById(R.id.bottomLayout);

// 根据性别设置背景颜色
        int tabMainColor = GenderResourceUtil.getTabMainColorRes(context);
        topLayout.setBackgroundColor(context.getResources().getColor(tabMainColor));
        bottomLayout.setBackgroundColor(context.getResources().getColor(tabMainColor));

        // 自动滚动到底部
        if (scrollView != null && isTimeReverse) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }

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
            // 去除换行和中括号
            String cleanLocation = location.replace("\n", "").replace("[", "").replace("]", "");
            // 只显示到县/区/市级
            String countyLevelLocation = extractCountyLevel(cleanLocation);
            tvLocationText.setText(countyLevelLocation);
            tvLocationText.setVisibility(View.VISIBLE);
            ivLocation.setVisibility(View.VISIBLE);
        } else {
            tvLocationText.setVisibility(View.GONE);
            ivLocation.setVisibility(View.GONE);
        }

        // 4. 处理正文内容
        tvContent.setText(!TextUtils.isEmpty(diary.getBody()) ? diary.getBody() : "无内容");

        // 动画期间禁止操作和返回
        View overlay = new View(context);
        overlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(0x00000000); // 透明
        overlay.setClickable(true);
        overlay.setFocusable(true);
        ((ViewGroup) view.getParent()).addView(overlay);

        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (isTimeReverse && keyCode == KeyEvent.KEYCODE_BACK) {
                ToastUtil.showMsg(context, "时光倒流中，请稍候...");
                return true; // 拦截返回键
            }
            return false; // 其他按键不处理
        });

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
            overlay.setVisibility(View.VISIBLE);
            startDeleteAnim(tvContent, new TextView[]{tvMoon, tvDay, tvWeek, tvTime, tvWeatherText, tvLocationText}, onDeleteAnimFinish, dialog, overlay);
        } else {
            overlay.setVisibility(View.GONE);
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


    private String extractCountyLevel(String address) {
        if (TextUtils.isEmpty(address)) return address;
        String[] suffixes = {"区", "县", "市"};
        int minIdx = -1;
        for (String suffix : suffixes) {
            int idx = address.indexOf(suffix);
            if (idx != -1) {
                if (minIdx == -1 || idx < minIdx) {
                    minIdx = idx;
                }
            }
        }
        if (minIdx != -1) {
            return address.substring(0, minIdx + 1);
        }
        return address;
    }
    // 逐步乱码+删除动画
    private void startDeleteAnim(TextView tvContent, TextView[] others, Runnable onFinish, Dialog dialog, View overlay) {
        Handler handler = new Handler();
        // 1. 正文逐步乱码
        Runnable garbleStep = new Runnable() {
            int garbleRounds = 0;
            String original = tvContent.getText().toString();
            String current = original;
            @Override
            public void run() {
                if (garbleRounds < 6) { // 6轮逐步乱码
                    current = garbleString(current); // 只传一个参数
                    tvContent.setText(current);
                    garbleRounds++;
                    handler.postDelayed(this, 80); // 逐步乱码过程
                } else {
                    // 2. 正文逐字删除
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (tvContent.getText().length() > 0) {
                                tvContent.setText(tvContent.getText().subSequence(0, tvContent.getText().length() - 1));
                                handler.postDelayed(this, 15); // 正文删除的速度
                            } else {
                                // 3. 其它字段一起乱码+删除
                                garbleAndDeleteOthers(others, onFinish, dialog, overlay);
                            }
                        }
                    }, 100);
                }
            }
        };
        handler.post(garbleStep);
    }

    // 其它字段乱码+删除
    private void garbleAndDeleteOthers(TextView[] fields, Runnable onFinish, Dialog dialog, View overlay) {
        Handler handler = new Handler();
        Runnable garbleStep = new Runnable() {
            int garbleRounds = 0;
            String[] originals = new String[fields.length];
            String[] currents = new String[fields.length];
            {
                for (int i = 0; i < fields.length; i++) {
                    originals[i] = fields[i].getText().toString();
                    currents[i] = originals[i];
                }
            }
            @Override
            public void run() {
                if (garbleRounds < 4) {
                    for (int i = 0; i < fields.length; i++) {
                        currents[i] = garbleString(currents[i]);
                        fields[i].setText(currents[i]);
                    }
                    garbleRounds++;
                    handler.postDelayed(this, 80);
                } else {
                    // 逐字删除
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            boolean hasText = false;
                            for (TextView field : fields) {
                                if (field.getText().length() > 0) {
                                    field.setText(field.getText().subSequence(0, field.getText().length() - 1));
                                    hasText = true;
                                }
                            }
                            if (hasText) {
                                handler.postDelayed(this, 30);// 其它字段删除的速度
                            } else {
                                dialog.dismiss();
                                overlay.setVisibility(View.GONE);
                                if (onFinish != null) onFinish.run();
                            }
                        }
                    }, 100);
                }
            }
        };
        handler.post(garbleStep);
    }

    /**
     * 将字符串部分或全部变为乱码（包含汉字、符号、特殊字符等）
     * @param s 原字符串
     * @return 变乱码后的字符串
     */
    private String garbleString(String s) {
        if (TextUtils.isEmpty(s)) return s;
        char[] arr = s.toCharArray();
        int n = arr.length;
        float ratio = 1.0f / 5.0f; // 固定为1/5
        int garbleCount = Math.max(1, (int)(n * ratio));
        java.util.Random r = new java.util.Random();
        String garbleChars = "ூ◊૭〒ミæœˆè|铿斤拷铿斤拷"
                + "▦▧▩";
//                + "abcdefghijklmnopqrstuvwxyz"
        for (int i = 0; i < garbleCount; i++) {
            int idx = r.nextInt(n);
            if (r.nextBoolean()) {
                arr[idx] = (char) (0x4e00 + r.nextInt(0x9fa5 - 0x4e00));
            } else {
                arr[idx] = garbleChars.charAt(r.nextInt(garbleChars.length()));
            }
        }
        return new String(arr);
    }



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
    // 根据天气索引获取天气文字描述
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