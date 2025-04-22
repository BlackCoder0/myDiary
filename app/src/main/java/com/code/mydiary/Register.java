package com.code.mydiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.code.mydiary.util.UserCRUD;
import com.code.mydiary.util.CaptchaView;

public class Register extends AppCompatActivity {

    private EditText edEmail, edPassword, edQcode;
    private RadioGroup rgSex;
    private Button btnRegister;
    private CaptchaView captchaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        edEmail = findViewById(R.id.ed_email);
        edPassword = findViewById(R.id.ed_password);
        edQcode = findViewById(R.id.ed_Qcode);
        rgSex = findViewById(R.id.rg_sex);
        btnRegister = findViewById(R.id.btn_tologin);
        captchaView = findViewById(R.id.captcha_view);

        captchaView.setOnClickListener(v -> captchaView.generateCode()); // 点击验证码刷新

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edEmail.getText().toString().trim();
                String password = edPassword.getText().toString().trim();
                int sex = rgSex.getCheckedRadioButtonId() == R.id.rb_male ? 1 : 2;
                String inputCode = edQcode.getText().toString().trim();

                // 图形验证码校验
                if (!inputCode.equalsIgnoreCase(captchaView.getCaptchaCode())) {
                    Toast.makeText(Register.this, "验证码错误", Toast.LENGTH_SHORT).show();
                    captchaView.generateCode();
                    return;
                }

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Register.this, "邮箱和密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserCRUD userCRUD = new UserCRUD(Register.this);
                userCRUD.open();
                if (userCRUD.isEmailExist(email)) {
                    Toast.makeText(Register.this, "该邮箱已注册", Toast.LENGTH_SHORT).show();
                    userCRUD.close();
                    return;
                }
                long userId = userCRUD.registerUser(email, password, sex);
                userCRUD.close();

                if (userId > 0) {
                    Toast.makeText(Register.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Register.this, Login.class));
                    finish();
                } else {
                    Toast.makeText(Register.this, "注册失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
