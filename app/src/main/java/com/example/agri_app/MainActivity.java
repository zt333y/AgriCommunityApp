package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

        TextView tvGoRegister = findViewById(R.id.tv_go_register);
        if (tvGoRegister != null) {
            tvGoRegister.setOnClickListener(v -> {
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

            RetrofitClient.getApi().login(new User(u, p)).enqueue(new Callback<Result<LoginResponse>>() {
                @Override
                public void onResponse(Call<Result<LoginResponse>> call, Response<Result<LoginResponse>> response) {
                    if (response.body() != null && response.body().code == 200) {

                        LoginResponse loginData = response.body().data;

                        // 初始化默认值
                        Long safeId = 1L;
                        String safeUsername = u;
                        String safeToken = "";
                        int safeRole = 0; // 🌟 新增：默认角色为居民(0)

                        if (loginData != null) {
                            // 1. 提取并激活 Token
                            if (loginData.getToken() != null) {
                                safeToken = loginData.getToken();
                                RetrofitClient.setToken(safeToken);
                            }

                            // 2. 提取用户信息及角色
                            if (loginData.getUser() != null) {
                                User user = loginData.getUser();
                                if (user.getId() != null) {
                                    safeId = user.getId();
                                }
                                if (user.getUsername() != null) {
                                    safeUsername = user.getUsername();
                                }
                                // 🌟 核心修改：提取后端返回的角色字段
                                if (user.getRole() != null) {
                                    safeRole = user.getRole();
                                }
                            }
                        }

                        // 3. 将重要信息持久化到手机本地缓存
                        // 注意：这里使用 "UserPrefs" 必须与 HomeActivity 中读取的名称一致
                        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putLong("userId", safeId);
                        editor.putString("username", safeUsername);
                        editor.putString("token", safeToken);
                        editor.putInt("role", safeRole); // 🌟 核心修改：存入角色信息
                        editor.apply();

                        Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        String errorMsg = (response.body() != null) ? response.body().msg : "未知错误";
                        Toast.makeText(MainActivity.this, "登录失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<LoginResponse>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}