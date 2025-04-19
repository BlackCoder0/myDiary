package com.code.mydiary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DiaryDatabase extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "diarys";
    public static final String ID = "id";
    public static final String TIME = "time";
    public static final String WEATHER = "weather";
    public static final String TEMPERATURE = "temperature";
    public static final String LOCATION = "location";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String MOOD = "mood";
    public static final String TAG = "tag";

    public DiaryDatabase(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE "+ TABLE_NAME
                +  "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TIME + " TEXT NOT NULL, "
                + WEATHER + " TEXT, "
                + TEMPERATURE + " TEXT, "
                + LOCATION + " TEXT, "
                + TITLE + " TEXT NOT NULL, "
                + BODY + " TEXT NOT NULL, "
                + MOOD + " INTEGER, "
                + TAG + " INTEGER DEFAULT 1)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARY);
//        onCreate(db);
//    }
//
//
//    // Delete a diary entry by ID
//    public void deleteDiary(long id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_DIARY, ID + " = ?", new String[]{String.valueOf(id)});
//    }
//
//
//    // Get all diary entries
//    public List<Diary> getAllDiaries() {
//        List<Diary> diaryList = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_DIARY, null, null, null, null, null, null);
//
//        if (cursor.moveToFirst()) {
//            do {
//                Diary diary = new Diary();
//                diary.setId(cursor.getLong(cursor.getColumnIndex(ID)));
//                diary.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
//                diary.setWeather(cursor.getString(cursor.getColumnIndex(WEATHER)));
//                diary.setTemperature(cursor.getString(cursor.getColumnIndex(TEMPERATURE)));
//                diary.setLocation(cursor.getString(cursor.getColumnIndex(LOCATION)));
//                diary.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
//                diary.setBody(cursor.getString(cursor.getColumnIndex(BODY)));
//                diary.setMood(cursor.getInt(cursor.getColumnIndex(MOOD)));
//                diary.setTag(cursor.getInt(cursor.getColumnIndex(TAG)));
//
//                diaryList.add(diary);
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        return diaryList;
//    }
//
//    // Get a single diary entry by ID

//
//    // Delete all diary entries
//    public void deleteAllDiaries() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_DIARY, null, null);
//    }
}