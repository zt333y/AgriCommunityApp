package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agri_app.network.RetrofitClient;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView tvUsername = findViewById(R.id.tv_profile_username);
        TextView btnGoToOrders = findViewById(R.id.btn_go_to_orders);
        Button btnLogout = findViewById(R.id.btn_logout);

        // 1. 从手机本地读取当前登录的用户名，显示在头像下方
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sp.getString("username", "未登录居民");
        tvUsername.setText(username);

        // 2. 点击“我的订单”，跳转到订单列表页
        btnGoToOrders.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, OrderActivity.class));
        });

        // 3. 💥 核心功能：安全退出登录
        btnLogout.setOnClickListener(v -> {
            // 第一步：清空手机里的账户信息缓存
            sp.edit().clear().apply();

            // 第二步：拔出插在全局网络请求里的 Token "钥匙"
            RetrofitClient.setToken("");

            Toast.makeText(this, "已安全退出系统", Toast.LENGTH_SHORT).show();

            // 第三步：跳回登录页
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            // 极其重要的一句：清除之前所有的页面记录，防止用户按手机的“返回键”又回到大厅
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
}