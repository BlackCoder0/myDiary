package com.code.mydiary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;

public class EditDiary extends AppCompatActivity {

    EditText Diary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_diary_activity);
        Diary=findViewById(R.id.edit_adddiary);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_HOME){
            return true;
        }
        else if (keyCode==KeyEvent.KEYCODE_BACK) {
            Intent intent=new Intent();
            intent.putExtra("input",Diary.getText().toString());
            setResult(RESULT_OK,intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
}