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
import android.widget.ImageView;
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
        TextView tv_day = v.findViewById(R.id.tv_day);
        TextView tv_week = v.findViewById(R.id.tv_week);

        // 天气与心情图标
        ImageView iv_weather = v.findViewById(R.id.iv_weather);
        ImageView iv_mood = v.findViewById(R.id.iv_mood);

        // 新增：标签图标
        ImageView iv_tag = v.findViewById(R.id.iv_tag);

        Diary diary = diaryList.get(position);

        tv_title.setText(!TextUtils.isEmpty(diary.getTitle()) ? diary.getTitle() : diary.getTime());
        tv_body.setText(!TextUtils.isEmpty(diary.getBody()) ? diary.getBody() : "无内容");

        // 设置标签图标状态
        if (diary.getTag() == 2) {
            iv_tag.setColorFilter(myContext.getResources().getColor(R.color.boy));
        } else {
            iv_tag.setColorFilter(myContext.getResources().getColor(R.color.gray));
        }
        iv_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切换tag状态
                if (diary.getTag() == 2) {
                    iv_tag.setColorFilter(myContext.getResources().getColor(R.color.gray));
                    diary.setTag(1);
                } else {
                    iv_tag.setColorFilter(myContext.getResources().getColor(R.color.boy));
                    diary.setTag(2);
                }
                // 持久化到数据库
                CRUD op = new CRUD(myContext);
                op.open();
                op.updataDiary(diary);
                op.close();
            }
        });

        // 设置天气图标
        int weather = diary.getWeather();
        if (weather == -1) {
            iv_weather.setVisibility(View.GONE);
        } else {
            int weatherResId = getWeatherIconResId(weather); // 传入正确的weather
            if (weatherResId != 0) {
                iv_weather.setImageResource(weatherResId);
                iv_weather.setVisibility(View.VISIBLE);
            } else {
                iv_weather.setVisibility(View.GONE);
            }
        }

        // 设置心情图标
        int mood = diary.getMood();
        if (mood == -1) {
            iv_mood.setVisibility(View.GONE);
        } else {
            int moodResId = getMoodIconResId(mood); // 自定义函数
            if (moodResId != 0) {
                iv_mood.setImageResource(moodResId);
                iv_mood.setVisibility(View.VISIBLE);
            } else {
                iv_mood.setVisibility(View.GONE);
            }
        }

        // 时间相关设置
        String timeStr = diary.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = sdf.parse(timeStr);

            SimpleDateFormat hmFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String hm = hmFormat.format(date);
            tv_time.setText(hm);

            SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
            tv_day.setText(dayFormat.format(date));

            SimpleDateFormat weekFormat = new SimpleDateFormat("EEE.", Locale.ENGLISH);
            tv_week.setText(weekFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
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
