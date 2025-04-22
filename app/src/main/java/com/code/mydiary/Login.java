package com.code.mydiary;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.code.mydiary.util.ToastUtil;


public class Login extends AppCompatActivity implements View.OnClickListener {

    //声明，初始化
    private Button myBtnLogin;
    private EditText myEdUsername;
    private EditText myEdPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity);

        //找到
        myBtnLogin = findViewById(R.id.btn_login);
        myEdUsername = findViewById(R.id.ed_email);
        myEdPassword = findViewById(R.id.ed_password);

        // 注册按钮跳转
        Button btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });

//        //直接跳转
//        myBtnLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = null;
//                intent = new Intent(Login.this,SelectByText.class);
//                startActivity(intent);
//            }
//        });

        //匹配对应的用户名和密码才跳转
        myBtnLogin.setOnClickListener(this);




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void onClick(View v){
        String username = myEdUsername.getText().toString();
        String password = myEdPassword.getText().toString();

        // 新增：用户表校验
        com.code.mydiary.util.UserCRUD userCRUD = new com.code.mydiary.util.UserCRUD(Login.this);
        userCRUD.open();
        long userId = userCRUD.login(username, password);
        userCRUD.close();

        if(userId > 0){
            ToastUtil.showMsg(Login.this,"登录成功");
            Intent intent = new Intent(Login.this, MainActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        }else{
            Toast toastCenter = Toast.makeText(getApplicationContext(),"账号密码有误，请重新输入",Toast.LENGTH_SHORT);
            toastCenter.setGravity(Gravity.CENTER,0,0);
            toastCenter.show();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}