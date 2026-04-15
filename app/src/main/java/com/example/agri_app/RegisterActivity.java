package com.example.agri_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agri_app.entity.Result;
import com.example.agri_app.entity.User;
import com.example.agri_app.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etUser = findViewById(R.id.et_reg_username);
        EditText etPhone = findViewById(R.id.et_reg_phone);
        EditText etPwd = findViewById(R.id.et_reg_password);
        EditText etConfirmPwd = findViewById(R.id.et_reg_confirm_password);
        Button btnSubmit = findViewById(R.id.btn_register_submit);

        btnSubmit.setOnClickListener(v -> {
            String u = etUser.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String p = etPwd.getText().toString().trim();
            String cp = etConfirmPwd.getText().toString().trim();

            // 1. 校验是否填完
            if (u.isEmpty() || phone.isEmpty() || p.isEmpty() || cp.isEmpty()) {
                Toast.makeText(this, "请填写完整信息（含手机号）", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. 🌟 手机号格式强校验
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                Toast.makeText(this, "⚠️ 手机号格式错误，请输入真实的11位手机号", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. 校验密码是否一致
            if (!p.equals(cp)) {
                Toast.makeText(this, "两次输入的密码不一致！", Toast.LENGTH_SHORT).show();
                return;
            }

            // 组装数据并发送请求
            User newUser = new User(u, p);
            newUser.setPhone(phone);
            newUser.setRole(0);
            RetrofitClient.getApi().register(newUser).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(RegisterActivity.this, "🎉 注册成功！请登录", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMsg = (response.body() != null) ? response.body().msg : "注册失败";
                        Toast.makeText(RegisterActivity.this, "注册失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}