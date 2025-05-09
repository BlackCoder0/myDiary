package com.code.mydiary.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class UserCRUD {
    private UserDatabase dbHelper;
    public SQLiteDatabase db;

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

    // 添加禁止事项
    public void addNoItem(long userId, String content) {
        ContentValues values = new ContentValues();
        values.put(UserDatabase.USER_ID, userId);
        values.put(UserDatabase.NO_CONTENT, content);
        db.insert(UserDatabase.NO_LIST_TABLE, null, values);
    }

    // 查询禁止事项
    public ArrayList<String> getNoList(long userId) {
        ArrayList<String> list = new ArrayList<>();
        Cursor cursor = db.query(UserDatabase.NO_LIST_TABLE, new String[]{UserDatabase.NO_CONTENT},
                UserDatabase.USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex(UserDatabase.NO_CONTENT)));
        }
        cursor.close();
        return list;
    }

    // 更新禁止事项
    public void updateNoItem(long userId, int index, String content) {
        Cursor cursor = db.query(UserDatabase.NO_LIST_TABLE, new String[]{UserDatabase.NO_ID},
                UserDatabase.USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);
        if (cursor.moveToPosition(index)) {
            int noId = cursor.getInt(cursor.getColumnIndex(UserDatabase.NO_ID));
            ContentValues values = new ContentValues();
            values.put(UserDatabase.NO_CONTENT, content);
            db.update(UserDatabase.NO_LIST_TABLE, values, UserDatabase.NO_ID + "=?", new String[]{String.valueOf(noId)});
        }
        cursor.close();
    }

    // 清空并重新插入（用于保存全部）
    public void saveNoList(long userId, ArrayList<String> list) {
        db.delete(UserDatabase.NO_LIST_TABLE, UserDatabase.USER_ID + "=?", new String[]{String.valueOf(userId)});
        for (String content : list) {
            if (!content.trim().isEmpty()) {
                addNoItem(userId, content);
            }
        }
    }

    // 添加紧急联系人
    public void addContact(long userId, String name, String phone) {
        ContentValues values = new ContentValues();
        values.put(UserDatabase.USER_ID, userId);
        values.put(UserDatabase.SOS_NAME, name);
        values.put(UserDatabase.SOS_PHONE, phone);
        db.insert(UserDatabase.SOS_TABLE, null, values);
    }

    // 查询紧急联系人
    public ArrayList<String[]> getContactList(long userId) {
        ArrayList<String[]> list = new ArrayList<>();
        Cursor cursor = db.query(UserDatabase.SOS_TABLE,
                new String[]{UserDatabase.SOS_NAME, UserDatabase.SOS_PHONE},
                UserDatabase.USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(UserDatabase.SOS_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(UserDatabase.SOS_PHONE));
            list.add(new String[]{name, phone});
        }
        cursor.close();
        return list;
    }

    // 删除紧急联系人
    public void deleteContact(long userId, String name, String phone) {
        db.delete(UserDatabase.SOS_TABLE,
                UserDatabase.USER_ID + "=? AND " +
                UserDatabase.SOS_NAME + "=? AND " +
                UserDatabase.SOS_PHONE + "=?",
                new String[]{String.valueOf(userId), name, phone});
    }

    public String getNoListTitle(long userId) {
        String title = "禁止事项 Ver.1";
        Cursor cursor = db.rawQuery(
            "SELECT " + UserDatabase.NO_TITLE + " FROM " + UserDatabase.NO_LIST_TABLE +
            " WHERE " + UserDatabase.USER_ID + "=? AND " + UserDatabase.NO_TITLE + " IS NOT NULL LIMIT 1",
            new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            String t = cursor.getString(0);
            if (t != null && !t.trim().isEmpty()) title = t;
        }
        cursor.close();
        return title;
    }

    public void saveNoListTitle(long userId, String title) {
        // 检查是否已存在该用户的标题行
        Cursor cursor = db.rawQuery(
            "SELECT " + UserDatabase.NO_ID + " FROM " + UserDatabase.NO_LIST_TABLE +
            " WHERE " + UserDatabase.USER_ID + "=? AND " + UserDatabase.NO_TITLE + " IS NOT NULL LIMIT 1",
            new String[]{String.valueOf(userId)});
        boolean exists = cursor.moveToFirst();
        long id = exists ? cursor.getLong(0) : -1;
        cursor.close();
    
        ContentValues values = new ContentValues();
        values.put(UserDatabase.USER_ID, userId);
        values.put(UserDatabase.NO_TITLE, title);
    
        if (exists) {
            db.update(UserDatabase.NO_LIST_TABLE, values, UserDatabase.NO_ID + "=?", new String[]{String.valueOf(id)});
        } else {
            // 插入一条仅有标题的记录
            db.insert(UserDatabase.NO_LIST_TABLE, null, values);
        }
    }
    // 根据userId获取性别
    public int getUserSex(long userId) {
        int sex = 1; // 默认男
        Cursor cursor = db.query(UserDatabase.USER_TABLE, new String[]{UserDatabase.SEX},
                UserDatabase.USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);
        if (cursor.moveToFirst()) {
            sex = cursor.getInt(cursor.getColumnIndex(UserDatabase.SEX));
        }
        cursor.close();
        return sex;
    }
}