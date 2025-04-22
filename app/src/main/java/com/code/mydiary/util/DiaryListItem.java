package com.code.mydiary.util;

import android.text.TextUtils;

import com.code.mydiary.Diary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiaryListItem {
    public static final int TYPE_MONTH_HEADER = 0;
    public static final int TYPE_DIARY = 1;

    public int type;
    public String monthText; // 仅type为0时用
    public Diary diary;      // 仅type为1时用

    public DiaryListItem(int type, String monthText, Diary diary) {
        this.type = type;
        this.monthText = monthText;
        this.diary = diary;
    }
}