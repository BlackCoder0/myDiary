package com.code.mydiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryAdopter extends BaseAdapter implements Filterable {
    private Context myContext;
    public List<Diary> bockList;//备份原始数据
    private List<Diary> diaryList;//会改变的数据
    private MyFilter myFilter;

    public DiaryAdopter(Context myContext, List<Diary> diaryList){
        this.myContext = myContext;
        this.diaryList = diaryList;
        bockList = diaryList;
//        this.bockList = new ArrayList<>(diaryList); // 深拷贝，避免引用同一个对象
    }

    @Override
    public int getCount(){
        return diaryList.size();
    }

    @Override
    public Object getItem(int position){
        return diaryList.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myContext);
        View v = View.inflate(myContext, R.layout.diary_layout, null);
        TextView tv_title = v.findViewById(R.id.tv_title);
        TextView tv_body = v.findViewById(R.id.tv_body);
        TextView tv_time = v.findViewById(R.id.tv_time);
        TextView tv_weather = v.findViewById(R.id.tv_weather);
        TextView tv_mood = v.findViewById(R.id.tv_mood);

        // 新增：获取tv_day和tv_week
        TextView tv_day = v.findViewById(R.id.tv_day);
        TextView tv_week = v.findViewById(R.id.tv_week);

        Diary diary = diaryList.get(position);

        tv_title.setText(!TextUtils.isEmpty(diary.getTitle()) ? diary.getTitle() : diary.getTime());
        tv_body.setText(!TextUtils.isEmpty(diary.getBody()) ? diary.getBody() : "无内容");
        tv_weather.setText(!TextUtils.isEmpty(diary.getWeather()) ? diary.getWeather() : "");
        tv_mood.setText(String.valueOf(diary.getMood()));

        String timeStr = diary.getTime(); // 例如 "2024-06-13 15:30:00"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = sdf.parse(timeStr);
            // 1. 时:分
            SimpleDateFormat hmFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String hm = hmFormat.format(date);
            tv_time.setText(hm);

            // 2. 几号
            SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
            String day = dayFormat.format(date);
            tv_day.setText(day);

            // 3. 星期几缩写
            SimpleDateFormat weekFormat = new SimpleDateFormat("EEE.", Locale.ENGLISH); // 英文缩写
            String week = weekFormat.format(date);
            tv_week.setText(week);

        } catch (ParseException e) {
            e.printStackTrace();
            // 设置默认值
            tv_time.setText("");
            tv_day.setText("");
            tv_week.setText("");
        }
        v.setTag(diary.getId());
        Log.d("DiaryAdopter", "getView: position=" + position + ", title=" + diary.getTitle());
        return v;
    }
    @Override
    public Filter getFilter(){
        if (myFilter == null){
            myFilter = new MyFilter();
        }
        return myFilter;
    }

    //定义过滤规则
    class MyFilter extends Filter{
        @Override
        protected FilterResults performFiltering(CharSequence charSequence){
            FilterResults result = new FilterResults();
            List<Diary> list;
            if (TextUtils.isEmpty(charSequence)){//当过滤的关键字为空，显示所有数据
                list = bockList;
            }else {
                list=new ArrayList<>();
                for(Diary diary:bockList){
                    if(diary.getBody().contains(charSequence)){
                        list.add(diary);
                    }
                }
            }
            result.values = list;//将得到的集合保存到FilterResults的value变量
            result.count = list.size();//将集合的大小保存到FilterResults的count变量

            return result;
        }


        //告诉适配器更新界面
        @Override
        protected void publishResults (CharSequence charSequence,FilterResults filterResults){
            diaryList = (List<Diary>)filterResults.values;
            if(filterResults.count>0){
                notifyDataSetChanged();//通知数据发生了改变
            }else {
                notifyDataSetInvalidated();//通知数据失效
            }
        }
    }




}
