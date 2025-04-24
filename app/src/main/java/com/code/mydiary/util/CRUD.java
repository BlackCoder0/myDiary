package com.code.mydiary.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.code.mydiary.Diary;

import java.util.ArrayList;
import java.util.List;

public class CRUD {
    SQLiteOpenHelper dbHandler;
    SQLiteDatabase db;

    private static final String[] columns={
            DiaryDatabase.ID,
            DiaryDatabase.TITLE,
            DiaryDatabase.BODY,
            DiaryDatabase.TIME,
            DiaryDatabase.WEATHER,
            DiaryDatabase.TEMPERATURE,
            DiaryDatabase.LOCATION,
            DiaryDatabase.MOOD,
            DiaryDatabase.TAG,
            DiaryDatabase.MODE
    };

    public CRUD(Context context){
        dbHandler=new DiaryDatabase(context);
    }

    public void open(){
        db=dbHandler.getWritableDatabase();
    }
    public void close(){
        dbHandler.close();
    }

    //diary → database
    public Diary addDiary(Diary diary){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DiaryDatabase.TIME, diary.getTime());
        contentValues.put(DiaryDatabase.WEATHER, diary.getWeather());
        contentValues.put(DiaryDatabase.TEMPERATURE, diary.getTemperature());
        contentValues.put(DiaryDatabase.LOCATION, diary.getLocation());
        contentValues.put(DiaryDatabase.TITLE, diary.getTitle());
        contentValues.put(DiaryDatabase.BODY, diary.getBody());
        contentValues.put(DiaryDatabase.MOOD, diary.getMood());
        contentValues.put(DiaryDatabase.TAG, diary.getTag());
        contentValues.put(DiaryDatabase.MODE, diary.getMode());
        long newId = db.insert(DiaryDatabase.TABLE_NAME,null,contentValues);
        diary.setId(newId);
        return diary;
    }

    public Diary getDiary(long id){
        Cursor cursor = db.query(DiaryDatabase.TABLE_NAME,columns,DiaryDatabase.ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        //String time, int weather, String temperature, String location, String title, String body, int mood, int tag, int mode
        Diary e = new Diary(cursor.getString(1),cursor.getInt(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getInt(7),cursor.getInt(8),cursor.getInt(9));
        return e;
    }

        public List<Diary> getAllDiary() {
        Cursor cursor = db.query(DiaryDatabase.TABLE_NAME, columns,null,null,null,null,null);
        List<Diary> diaries = new ArrayList<>();
        if (cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                Diary diary = new Diary();
                diary.setId(cursor.getLong(cursor.getColumnIndex(DiaryDatabase.ID)));
                diary.setTime(cursor.getString(cursor.getColumnIndex(DiaryDatabase.TIME)));
                diary.setWeather(cursor.getInt(cursor.getColumnIndex(DiaryDatabase.WEATHER)));
                diary.setTemperature(cursor.getString(cursor.getColumnIndex(DiaryDatabase.TEMPERATURE)));
                diary.setLocation(cursor.getString(cursor.getColumnIndex(DiaryDatabase.LOCATION)));
                diary.setTitle(cursor.getString(cursor.getColumnIndex(DiaryDatabase.TITLE)));
                diary.setBody(cursor.getString(cursor.getColumnIndex(DiaryDatabase.BODY)));
                diary.setMood(cursor.getInt(cursor.getColumnIndex(DiaryDatabase.MOOD)));
                diary.setTag(cursor.getInt(cursor.getColumnIndex(DiaryDatabase.TAG)));
                diary.setMode(cursor.getInt(cursor.getColumnIndex(DiaryDatabase.MODE)));
                diaries.add(diary);
            }
        }
        return diaries;
    }

    public int updataDiary(Diary diary){
        ContentValues values = new ContentValues();
        values.put(DiaryDatabase.TIME, diary.getTime());
        values.put(DiaryDatabase.WEATHER, diary.getWeather());
        values.put(DiaryDatabase.TEMPERATURE, diary.getTemperature());
        values.put(DiaryDatabase.LOCATION, diary.getLocation());
        values.put(DiaryDatabase.TITLE, diary.getTitle());
        values.put(DiaryDatabase.BODY, diary.getBody());
        values.put(DiaryDatabase.MOOD, diary.getMood());
        values.put(DiaryDatabase.TAG, diary.getTag());
        values.put(DiaryDatabase.MODE, diary.getMode());

        return db.update(DiaryDatabase.TABLE_NAME, values, DiaryDatabase.ID + " = ?",
                new String[]{String.valueOf(diary.getId())});
    }

        public void deleteDiary(Diary diary) {
//        SQLiteDatabase db = DiaryDatabase.getWritableDatabase();
        db.delete(DiaryDatabase.TABLE_NAME, DiaryDatabase.ID + "=" + diary.getId(), null);
    }

    public void deleteDiaryById(long id) {
        db.delete(DiaryDatabase.TABLE_NAME, DiaryDatabase.ID + "=?", new String[]{String.valueOf(id)});
    }


}
