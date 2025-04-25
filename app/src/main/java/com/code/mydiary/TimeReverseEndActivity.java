package com.code.mydiary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.code.mydiary.util.ToastUtil;

public class TimeReverseEndActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timereverse_end_activity);
        // 显示 Toast 提示
        ToastUtil.showMsg(this, "点击返回键可恢复正常");
    }

    @Override
    public void onBackPressed() {
        // 返回 MainActivity 并恢复日记
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 清除 Activity 栈
        intent.putExtra("restoreDiaries", true); // 添加恢复标志
        startActivity(intent);
        finish();
    }
}