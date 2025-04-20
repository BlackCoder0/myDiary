package com.code.mydiary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddDiary extends AppCompatActivity {

    EditText DiaryTitle;
    EditText DiaryBody;
    private String addDiary_title;
    private String addDiary_body;
    private String addDiary_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_diary_activity);
        DiaryTitle=findViewById(R.id.edit_adddiary_title);
        DiaryBody=findViewById(R.id.edit_adddiary_body);
        Log.d("AddDiary", "onCreate is called");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_HOME){
            return true;
        }
        else if (keyCode==KeyEvent.KEYCODE_BACK) {
            Intent intent=new Intent();
            intent.putExtra("addDiary_title",DiaryTitle.getText().toString());
            intent.putExtra("addDiary_body",DiaryBody.getText().toString());
            intent.putExtra("addDiary_time",dataToStr());
            setResult(RESULT_OK,intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    public String dataToStr(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}