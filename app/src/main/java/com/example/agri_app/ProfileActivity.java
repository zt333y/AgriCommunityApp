package com.example.agri_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Button btnLogout = findViewById(R.id.btn_logout);

        // 退出登录的点击事件
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "已安全退出登录", Toast.LENGTH_SHORT).show();

            // 跳转回登录页面
            Intent intent = new Intent(this, MainActivity.class);
            // 重点：加上这两行标志位，可以清空之前打开的所有页面。这样按手机的“返回键”就不会又回到主页了
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
}