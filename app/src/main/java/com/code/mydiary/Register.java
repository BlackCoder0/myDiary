package com.code.mydiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;


import androidx.appcompat.app.AppCompatActivity;


import com.code.mydiary.util.ToastUtil;
import com.code.mydiary.util.UserCRUD;
import com.code.mydiary.util.CaptchaView;

public class Register extends AppCompatActivity {

    private EditText edEmail, edPassword, edQcode;
    private RadioGroup rgSex;
    private Button btnRegister;
    private CaptchaView captchaView;
    public ToastUtil toastUtil;

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
                String inputCode = edQcode.getText().toString().trim();
                int checkedSexId = rgSex.getCheckedRadioButtonId();

                // 新增：校验所有必填项
                if (email.isEmpty()) {
                    toastUtil.showMsg(Register.this, "请输入邮箱");
                    return;
                }
                if (password.isEmpty()) {
                    toastUtil.showMsg(Register.this, "请输入密码");
                    return;
                }
                if (inputCode.isEmpty()) {
                    toastUtil.showMsg(Register.this, "请输入验证码");
                    return;
                }
                if (checkedSexId == -1) {
                    toastUtil.showMsg(Register.this, "请选择性别");
                    return;
                }

                int sex = checkedSexId == R.id.rb_male ? 1 : 2;

                // 图形验证码校验
                if (!inputCode.equalsIgnoreCase(captchaView.getCaptchaCode())) {
                    toastUtil.showMsg(Register.this, "验证码错误");
                    captchaView.generateCode();
                    return;
                }

                UserCRUD userCRUD = new UserCRUD(Register.this);
                userCRUD.open();
                if (userCRUD.isEmailExist(email)) {
                    toastUtil.showMsg(Register.this, "该邮箱已注册");
                    userCRUD.close();
                    return;
                }
                long userId = userCRUD.registerUser(email, password, sex);
                userCRUD.close();

                if (userId > 0) {
                    toastUtil.showMsg(Register.this, "注册成功，请登录");
                    startActivity(new Intent(Register.this, Login.class));
                    finish();
                } else {
                    toastUtil.showMsg(Register.this, "注册失败");
                }
            }
        });
    }
}
