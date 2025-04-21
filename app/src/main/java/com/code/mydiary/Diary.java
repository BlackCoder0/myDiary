package com.code.mydiary;

public class Diary {
    private long id;
    private String time;  // 时间：年、月、日;时、分;月份，星期几
    private int weather;  // 天气
    private String temperature;  // 温度
    private String location;  // 位置
    private String title;  // 标题
    private String body;  // 正文
    private int mood;  // 心情
    private int tag;   // 标签

    public Diary() {
    }

    public Diary(String time, int weather, String temperature, String location, String title, String body, int mood, int tag) {
        this.time = time;
        this.weather = weather;
        this.temperature = temperature;
        this.location = location;
        this.title = title;
        this.body = body;
        this.mood = mood;
        this.tag = tag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getMood() {
        return mood;
    }

    public void setMood(int mood) {
        this.mood = mood;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "Diary{" +
                "id=" + id +
                ", time='" + time + '\'' +
                ", weather='" + weather + '\'' +
                ", temperature='" + temperature + '\'' +
                ", location='" + location + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", mood=" + mood +
                ", tag=" + tag +
                '}';
    }

//    @Override
//    public String toString() {
//    return body + "\n" + time.substring(5,16) + " "+ id;
//    }

}