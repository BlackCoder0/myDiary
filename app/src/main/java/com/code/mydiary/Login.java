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
        //获取
        String username = myEdUsername.getText().toString();
        String password = myEdPassword.getText().toString();
        //弹出的内容
        String ok = "登录成功";
        String fail = "账号密码有误，请重新输入";

        Intent intent = null;

        //正确账号：999@qq.com,密码:123456
        if(username.equals("999@qq.com")&&password.equals("123456")){
            //Toast.makeText(getApplicationContext(),ok,Toast.LENGTH_SHORT).show();
            ToastUtil.showMsg(Login.this,ok);

            //正确，跳转
            intent=new Intent(Login.this,SelectByText.class);
            startActivity(intent);
        }else{
            //不正确,居中弹窗
            Toast toastCenter = Toast.makeText(getApplicationContext(),fail,Toast.LENGTH_SHORT);
            toastCenter.setGravity(Gravity.CENTER,0,0);
            toastCenter.show();

        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}