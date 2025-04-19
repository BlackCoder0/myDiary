package com.code.mydiary;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);
        Log.d("ClickDebug", "Menu Activity 启动");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("ClickDebug", "返回键被按下");
    }
}