package com.code.mydiary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

public class AddDiary extends AppCompatActivity {

    EditText DiaryTitle;
    EditText DiaryBody;

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
            setResult(RESULT_OK,intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
}