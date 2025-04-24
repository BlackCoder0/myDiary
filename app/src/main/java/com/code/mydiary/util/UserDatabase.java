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
    public static final String NO_LIST_TABLE = "no_list";
    public static final String NO_ID = "no_id";
    public static final String NO_CONTENT = "no_content";
    public static final String SOS_TABLE = "sos_table";
    public static final String SOS_ID = "sos_id";
    public static final String SOS_NAME = "sos_name";
    public static final String SOS_PHONE = "sos_phone";

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

        db.execSQL("CREATE TABLE " + NO_LIST_TABLE + " (" +
                NO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_ID + " INTEGER NOT NULL, " +
                NO_CONTENT + " TEXT)");

        db.execSQL("CREATE TABLE " + SOS_TABLE + " (" +
                SOS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_ID + " INTEGER NOT NULL, " +
                SOS_NAME + " TEXT, " +
                SOS_PHONE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 这里暂不实现
    }
}