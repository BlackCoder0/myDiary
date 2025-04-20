package com.code.mydiary;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DiaryAdopter extends BaseAdapter implements Filterable {
    private Context myContext;
    private List<Diary> bockList;//备份原始数据
    private List<Diary> diaryList;//会改变的数据
    private MyFilter myFilter;

    public DiaryAdopter(Context myContext,List<Diary> diaryList){
        this.myContext = myContext;
        this.diaryList = diaryList;
        bockList = diaryList;
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
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myContext);
//        myContext.setTheme((sharedPreferences.getBoolean("girlMode",false)? R.style.GirlTheme:R.style.BoyTheme));
        View v = View.inflate(myContext,R.layout.diary_layout,null);
        TextView tv_title = (TextView)v.findViewById(R.id.tv_title);
        TextView tv_body = (TextView)v.findViewById(R.id.tv_body);
        TextView tv_time = (TextView)v.findViewById(R.id.tv_time);
        TextView tv_weather = (TextView)v.findViewById(R.id.tv_weather);
        TextView tv_mood = (TextView)v.findViewById(R.id.tv_mood);

        String Text_title = diaryList.get(position).getTitle();
        String Text_body = diaryList.get(position).getBody();
        /*
        if(sharedPreferences.getBoolean("diaryTitle",true))
            tv_body.setText(tv_body.split("\n")[0]);
        else tv_body.setText(Text_body);
         */
//        if(sharedPreferences.getBoolean("diaryTitle",true))
//            tv_body.setText(Text_title);

        if(Text_title != null && !Text_title.isEmpty())
           tv_body.setText(Text_title);
        tv_body.setText(Text_body);
//        if(sharedPreferences.getBoolean("diaryWeather",true))
//            tv_weather.setText(tv_weather);
//        if(sharedPreferences.getBoolean("diaryMood",true))
//            tv_mood.setText(tv_mood);
        tv_time.setText(diaryList.get(position).getTime());

        v.setTag(diaryList.get(position).getId());
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
