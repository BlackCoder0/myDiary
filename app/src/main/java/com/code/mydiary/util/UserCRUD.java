package com.code.mydiary.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserCRUD {
    private UserDatabase dbHelper;
    private SQLiteDatabase db;

    public UserCRUD(Context context) {
        dbHelper = new UserDatabase(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // 注册用户
    public long registerUser(String email, String password, int sex) {
        ContentValues values = new ContentValues();
        values.put(UserDatabase.EMAIL, email);
        values.put(UserDatabase.PASSWORD, password);
        values.put(UserDatabase.SEX, sex);
        values.put(UserDatabase.LOVER, -1);
        return db.insert(UserDatabase.USER_TABLE, null, values);
    }

    // 检查邮箱是否已注册
    public boolean isEmailExist(String email) {
        Cursor cursor = db.query(UserDatabase.USER_TABLE, null, UserDatabase.EMAIL + "=?", new String[]{email}, null, null, null);
        boolean exist = cursor.moveToFirst();
        cursor.close();
        return exist;
    }

    // 登录校验
    public long login(String email, String password) {
        Cursor cursor = db.query(UserDatabase.USER_TABLE, new String[]{UserDatabase.USER_ID}, UserDatabase.EMAIL + "=? AND " + UserDatabase.PASSWORD + "=?", new String[]{email, password}, null, null, null);
        long userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndex(UserDatabase.USER_ID));
        }
        cursor.close();
        return userId;
    }

    // 关联用户和日记
    public void linkUserDiary(long userId, long diaryId) {
        ContentValues values = new ContentValues();
        values.put(UserDatabase.USER_ID, userId);
        values.put(UserDatabase.DIARY_ID, diaryId);
        db.insert(UserDatabase.USER_DIARY_TABLE, null, values);
    }

    // 查询用户自己的日记ID列表
    public Cursor getUserDiaryIds(long userId) {
        return db.query(UserDatabase.USER_DIARY_TABLE, new String[]{UserDatabase.DIARY_ID}, UserDatabase.USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
    }
}