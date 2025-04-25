package com.code.mydiary.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.code.mydiary.Diary;
import com.code.mydiary.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryAdopter extends BaseAdapter {
    private Context myContext;
    private List<DiaryListItem> data;
    private LayoutInflater inflater;

    public DiaryAdopter(Context myContext, List<DiaryListItem> data){
        this.myContext = myContext;
        this.data = data;
        this.inflater = LayoutInflater.from(myContext);
    }

    public void setData(List<DiaryListItem> data) {
        this.data = data;
    }

    @Override
    public int getCount() { return data.size(); }

    @Override
    public Object getItem(int position) { return data.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }

    @Override
    public int getViewTypeCount() { return 2; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        if (type == DiaryListItem.TYPE_MONTH_HEADER) {
            MonthHeaderHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_month_header, parent, false);
                holder = new MonthHeaderHolder();
                holder.tvMonth = convertView.findViewById(R.id.tv_month_header);
                convertView.setTag(holder);
            } else {
                holder = (MonthHeaderHolder) convertView.getTag();
            }
            holder.tvMonth.setText(data.get(position).monthText);
            return convertView;
        } else {
            DiaryHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.diary_layout, parent, false);
                holder = new DiaryHolder();
                holder.tvTitle = convertView.findViewById(R.id.tv_title);
                holder.tvBody = convertView.findViewById(R.id.tv_body);
                holder.tvTime = convertView.findViewById(R.id.tv_time);
                holder.tvDay = convertView.findViewById(R.id.tv_day);
                holder.tvWeek = convertView.findViewById(R.id.tv_week);
                holder.ivWeather = convertView.findViewById(R.id.iv_weather);
                holder.ivMood = convertView.findViewById(R.id.iv_mood);
                holder.ivTag = convertView.findViewById(R.id.iv_tag);
                convertView.setTag(holder);
            } else {
                holder = (DiaryHolder) convertView.getTag();
            }
            Diary diary = data.get(position).diary;
            // holder.tvTitle.setText(!TextUtils.isEmpty(diary.getTitle()) ? diary.getTitle() : diary.getTime());
            if (!TextUtils.isEmpty(diary.getTitle())) {
                holder.tvTitle.setText(diary.getTitle());
            } else {
                // 只显示年月日
                String timeStr = diary.getTime();
                String dateOnly = timeStr;
                if (timeStr != null && timeStr.length() >= 10) {
                    dateOnly = timeStr.substring(0, 10); // yyyy-MM-dd
                }
                holder.tvTitle.setText(dateOnly);
            }
            holder.tvBody.setText(!TextUtils.isEmpty(diary.getBody()) ? diary.getBody() : "无内容");

            // 标签图标
            if (diary.getTag() == 2) {
                holder.ivTag.setColorFilter(myContext.getResources().getColor(
                        com.code.mydiary.util.GenderResourceUtil.getTabMainColorRes(myContext)
                ));
                holder.ivTag.setAlpha(1.0f);
            } else {
                holder.ivTag.setColorFilter(myContext.getResources().getColor(R.color.gray));
                holder.ivTag.setAlpha(0.5f); // 灰色且半透明
            }
            holder.ivTag.setOnClickListener(v -> {
                Log.d("DiaryAdopter", "Tag clicked, old tag=" + diary.getTag());
                if (diary.getTag() == 2) {
                    holder.ivTag.setColorFilter(myContext.getResources().getColor(R.color.gray));
                    holder.ivTag.setAlpha(1.0f);
                    diary.setTag(1);
                } else {
                    // 根据性别设置颜色
                    holder.ivTag.setColorFilter(myContext.getResources().getColor(
                            com.code.mydiary.util.GenderResourceUtil.getTabMainColorRes(myContext)
                    ));
                    holder.ivTag.setAlpha(0.5f);
                    diary.setTag(2);
                }
                Log.d("DiaryAdopter", "Tag after click=" + diary.getTag());
                CRUD op = new CRUD(myContext);
                op.open();
                op.updataDiary(diary);
                op.close();
                notifyDataSetChanged();
            });

            // 天气图标
            int weather = diary.getWeather();
            if (weather == -1) {
                holder.ivWeather.setColorFilter(myContext.getResources().getColor(R.color.gray));
                holder.ivWeather.setAlpha(0.5f);
                holder.ivWeather.setImageResource(R.drawable.ic_weather_sunny); // 可选：默认图标
            } else {
                int weatherResId = getWeatherIconResId(weather);
                if (weatherResId != 0) {
                    holder.ivWeather.setImageResource(weatherResId);
                    holder.ivWeather.setColorFilter(myContext.getResources().getColor(
                            com.code.mydiary.util.GenderResourceUtil.getTabMainColorRes(myContext)
                    ));
                    holder.ivWeather.setAlpha(1.0f);
                }
            }

            // 心情图标
            int mood = diary.getMood();
            if (mood == -1) {
                holder.ivMood.setColorFilter(myContext.getResources().getColor(R.color.gray));
                holder.ivMood.setAlpha(0.5f);
                holder.ivMood.setImageResource(R.drawable.ic_mood_happy); // 可选：默认图标
            } else {
                int moodResId = getMoodIconResId(mood);
                if (moodResId != 0) {
                    holder.ivMood.setImageResource(moodResId);
                    holder.ivMood.setColorFilter(myContext.getResources().getColor(
                            com.code.mydiary.util.GenderResourceUtil.getTabMainColorRes(myContext)
                    ));
                    holder.ivMood.setAlpha(1.0f);
                }
            }

            int textColor = myContext.getResources().getColor(
                    com.code.mydiary.util.GenderResourceUtil.getTabMainColorRes(myContext)
            );
            holder.tvDay.setTextColor(textColor);
            holder.tvWeek.setTextColor(textColor);
            holder.tvTime.setTextColor(textColor);
            holder.tvTitle.setTextColor(textColor);
            holder.tvBody.setTextColor(textColor);

            // 时间相关
            String timeStr = diary.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date date = sdf.parse(timeStr);
                if (date != null) {
                    SimpleDateFormat hmFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    holder.tvTime.setText(hmFormat.format(date));
                    SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
                    holder.tvDay.setText(dayFormat.format(date));
                    SimpleDateFormat weekFormat = new SimpleDateFormat("EEE.", Locale.ENGLISH);
                    holder.tvWeek.setText(weekFormat.format(date));
                } else {
                    throw new ParseException("Parsed date is null", 0);
                }
            } catch (ParseException | NullPointerException e) {
                Log.e("DiaryAdopter", "Error parsing date string: " + timeStr, e);
                holder.tvTime.setText("");
                holder.tvDay.setText("");
                holder.tvWeek.setText("");
            }
            return convertView;
        }
    }

    static class MonthHeaderHolder {
        TextView tvMonth;
    }
    static class DiaryHolder {
        TextView tvTitle;
        TextView tvBody;
        TextView tvTime;
        TextView tvDay;
        TextView tvWeek;
        ImageView ivWeather;
        ImageView ivMood;
        ImageView ivTag;
    }

    // 你原有的 getWeatherIconResId/getMoodIconResId 方法保留
    private int getWeatherIconResId(int weather) {
        switch (weather) {
            case 0: return R.drawable.ic_weather_cloud;
            case 1: return R.drawable.ic_weather_foggy;
            case 2: return R.drawable.ic_weather_rainy;
            case 3: return R.drawable.ic_weather_snowy;
            case 4: return R.drawable.ic_weather_sunny;
            case 5: return R.drawable.ic_weather_windy;
            // 添加更多 weather 类型映射
            default: return 0;
        }
    }
    private int getMoodIconResId(int mood) {
        switch (mood) {
            case 0: return R.drawable.ic_mood_happy;
            case 1: return R.drawable.ic_mood_soso;
            case 2: return R.drawable.ic_mood_unhappy;
            // 可继续拓展更多心情等级
            default: return 0;
        }
    }
}
