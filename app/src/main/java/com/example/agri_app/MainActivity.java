package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // 🌟 引入了 TextView
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.entity.LoginResponse;
import com.example.agri_app.entity.Result;
import com.example.agri_app.entity.User;
import com.example.agri_app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etUser = findViewById(R.id.et_username);
        EditText etPwd = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);

        // 🌟 新增：绑定“去注册”文字的点击事件
        TextView tvGoRegister = findViewById(R.id.tv_go_register);
        if (tvGoRegister != null) {
            tvGoRegister.setOnClickListener(v -> {
                // 跳转到注册页面
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            });
        }

        btnLogin.setOnClickListener(v -> {
            String u = etUser.getText().toString();
            String p = etPwd.getText().toString();

            if(u.isEmpty() || p.isEmpty()){
                Toast.makeText(this, "账号密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 发起登录请求，返回类型必须匹配 ApiService 中的 Result<LoginResponse>
            RetrofitClient.getApi().login(new User(u, p)).enqueue(new Callback<Result<LoginResponse>>() {
                @Override
                public void onResponse(Call<Result<LoginResponse>> call, Response<Result<LoginResponse>> response) {
                    if (response.body() != null && response.body().code == 200) {

                        LoginResponse loginData = response.body().data;

                        // 【安全防御模式】：初始化默认值，防止后端返回数据缺失导致 App 闪退
                        Long safeId = 1L; // 默认 ID
                        String safeUsername = u;
                        String safeToken = "";

                        // 安全地解析嵌套的 LoginResponse 数据
                        if (loginData != null) {
                            // 1. 提取并激活 Token（解决“请求被拦截”问题的关键）
                            if (loginData.getToken() != null) {
                                safeToken = loginData.getToken();
                                // 将 Token 注入到 Retrofit 全局拦截器中
                                RetrofitClient.setToken(safeToken);
                            }

                            // 2. 提取用户信息
                            if (loginData.getUser() != null) {
                                if (loginData.getUser().getId() != null) {
                                    safeId = loginData.getUser().getId();
                                }
                                if (loginData.getUser().getUsername() != null) {
                                    safeUsername = loginData.getUser().getUsername();
                                }
                            }
                        }

                        // 3. 将重要信息持久化到手机本地缓存
                        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putLong("userId", safeId);
                        editor.putString("username", safeUsername);
                        editor.putString("token", safeToken);
                        editor.apply();

                        Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                        // 4. 跳转到商品大厅（HomeActivity）
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish(); // 销毁登录页，防止用户按返回键回到登录界面
                    } else {
                        // 登录业务失败处理
                        String errorMsg = (response.body() != null) ? response.body().msg : "未知错误";
                        Toast.makeText(MainActivity.this, "登录失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<LoginResponse>> call, Throwable t) {
                    // 网络请求失败处理（如服务器未启动或 IP 错误）
                    Toast.makeText(MainActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}