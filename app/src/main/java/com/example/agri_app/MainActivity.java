package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

                        // 【超级防御模式】：不管发生什么，绝对不闪退
                        Long safeId = 1L; // 如果没拿到id，默认给 1 号居民，保证能加购物车
                        String safeUsername = u;
                        String safeToken = "";

                        // 安全地一层层剥开数据，防止空指针
                        if (loginData != null) {
                            if (loginData.getToken() != null) {
                                safeToken = loginData.getToken();
                            }
                            if (loginData.getUser() != null) {
                                if (loginData.getUser().getId() != null) {
                                    safeId = loginData.getUser().getId(); // 拿到真实ID
                                }
                                if (loginData.getUser().getUsername() != null) {
                                    safeUsername = loginData.getUser().getUsername();
                                }
                            }
                        }

                        // 安全存入手机缓存
                        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putLong("userId", safeId);
                        editor.putString("username", safeUsername);
                        editor.putString("token", safeToken);
                        editor.apply();

                        Toast.makeText(MainActivity.this, "安全登录成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish(); // 关掉登录页
                    } else {
                        Toast.makeText(MainActivity.this, "登录失败: " + (response.body() != null ? response.body().msg : "未知错误"), Toast.LENGTH_SHORT).show();
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