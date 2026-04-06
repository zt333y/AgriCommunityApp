package com.example.agri_app;

import android.content.Intent;
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

            RetrofitClient.getApi().login(new User(u, p)).enqueue(new Callback<Result<User>>() {
                @Override
                public void onResponse(Call<Result<User>> call, Response<Result<User>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        // 登录成功，跳转到商品大厅！
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish(); // 关掉登录页
                    } else {
                        Toast.makeText(MainActivity.this, "失败: " + (response.body() != null ? response.body().msg : "未知"), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Result<User>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}