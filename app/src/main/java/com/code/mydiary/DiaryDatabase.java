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

    public static final String MODE = "mode";

    public DiaryDatabase(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE "+ TABLE_NAME
                +  "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TIME + " TEXT NOT NULL, "
                + WEATHER + " INTEGER DEFAULT -1, "
                + TEMPERATURE + " TEXT, "
                + LOCATION + " TEXT, "
                + TITLE + " TEXT NOT NULL, "
                + BODY + " TEXT NOT NULL, "
                + MOOD + " INTEGER DEFAULT -1, "
                + TAG + " INTEGER DEFAULT 1,"
                + MODE + " INTEGER DEFAULT 1)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}