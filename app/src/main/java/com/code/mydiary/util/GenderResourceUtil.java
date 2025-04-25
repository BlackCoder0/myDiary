package com.code.mydiary.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.code.mydiary.R;

public class GenderResourceUtil {
    // 获取当前用户性别（1=男，2=女）
    public static int getCurrentUserSex(Context context) {
        SharedPreferences sp = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        return sp.getInt("sex", 1); // 默认男
    }

    // 获取主界面背景
    public static int getMainBackgroundRes(Context context) {
        int sex = getCurrentUserSex(context);
        return sex == 2 ? R.drawable.theme_bg_mitsuha : R.drawable.theme_bg_taki;
    }

    // 获取底部导航栏颜色
    public static int getBottomBarColorRes(Context context) {
        int sex = getCurrentUserSex(context);
        return sex == 2 ? R.color.girl : R.color.boy;
    }

    // 获取tab主色
    public static int getTabMainColorRes(Context context) {
        int sex = getCurrentUserSex(context);
        return sex == 2 ? R.color.girl : R.color.boy;
    }
}