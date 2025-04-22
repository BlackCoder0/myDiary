package com.code.mydiary.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabase extends SQLiteOpenHelper {

    public static final String DB_NAME = "user_db";
    public static final String USER_TABLE = "users";
    public static final String USER_DIARY_TABLE = "user_diary";
    public static final String USER_ID = "user_id";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String SEX = "sex";
    public static final String LOVER = "lover";
    public static final String DIARY_ID = "diary_id";

    public UserDatabase(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 用户表
        db.execSQL("CREATE TABLE " + USER_TABLE + " (" +
                USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EMAIL + " TEXT UNIQUE NOT NULL, " +
                PASSWORD + " TEXT NOT NULL, " +
                SEX + " INTEGER, " +
                LOVER + " INTEGER DEFAULT -1)");

        // 用户-日记关联表
        db.execSQL("CREATE TABLE " + USER_DIARY_TABLE + " (" +
                USER_ID + " INTEGER NOT NULL, " +
                DIARY_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY (" + USER_ID + ", " + DIARY_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 这里暂不实现
    }
}