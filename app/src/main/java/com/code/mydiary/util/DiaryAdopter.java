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
            holder.tvTitle.setText(!TextUtils.isEmpty(diary.getTitle()) ? diary.getTitle() : diary.getTime());
            holder.tvBody.setText(!TextUtils.isEmpty(diary.getBody()) ? diary.getBody() : "无内容");

            // 标签图标
            if (diary.getTag() == 2) {
                holder.ivTag.setColorFilter(myContext.getResources().getColor(R.color.boy));
            } else {
                holder.ivTag.setColorFilter(myContext.getResources().getColor(R.color.gray));
            }
            holder.ivTag.setOnClickListener(v -> {
                if (diary.getTag() == 2) {
                    holder.ivTag.setColorFilter(myContext.getResources().getColor(R.color.gray));
                    diary.setTag(1);
                } else {
                    holder.ivTag.setColorFilter(myContext.getResources().getColor(R.color.boy));
                    diary.setTag(2);
                }
                CRUD op = new CRUD(myContext);
                op.open();
                op.updataDiary(diary);
                op.close();
            });

            // 天气图标
            int weather = diary.getWeather();
            if (weather == -1) {
                holder.ivWeather.setVisibility(View.GONE);
            } else {
                int weatherResId = getWeatherIconResId(weather);
                if (weatherResId != 0) {
                    holder.ivWeather.setImageResource(weatherResId);
                    holder.ivWeather.setVisibility(View.VISIBLE);
                } else {
                    holder.ivWeather.setVisibility(View.GONE);
                }
            }

            // 心情图标
            int mood = diary.getMood();
            if (mood == -1) {
                holder.ivMood.setVisibility(View.GONE);
            } else {
                int moodResId = getMoodIconResId(mood);
                if (moodResId != 0) {
                    holder.ivMood.setImageResource(moodResId);
                    holder.ivMood.setVisibility(View.VISIBLE);
                } else {
                    holder.ivMood.setVisibility(View.GONE);
                }
            }

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
        // ... 根据你的项目资源返回对应的图标ID ...
        return 0;
    }
    private int getMoodIconResId(int mood) {
        // ... 根据你的项目资源返回对应的图标ID ...
        return 0;
    }
}
