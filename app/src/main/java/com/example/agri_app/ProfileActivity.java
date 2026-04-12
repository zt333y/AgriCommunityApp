package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. 初始化控件
        TextView tvUsername = findViewById(R.id.tv_username);
        TextView tvRoleBadge = findViewById(R.id.tv_role_badge);
        TextView btnMyOrders = findViewById(R.id.btn_my_orders);
        TextView btnMyCart = findViewById(R.id.btn_my_cart);

        // 核心管理入口 (复用 btn_my_products 控件 ID)
        TextView btnManagement = findViewById(R.id.btn_my_products);
        View lineFarmer = findViewById(R.id.line_farmer);

        Button btnLogout = findViewById(R.id.btn_logout);

        // 2. 从本地缓存 UserPrefs 中抓取登录信息
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sp.getString("username", "农产品用户");
        // 修正后的角色逻辑映射：0居民, 1农户, 2团长, 3管理员
        int role = sp.getInt("role", 0);

        // 3. 渲染基础信息
        tvUsername.setText(username);

        // 初始隐藏管理入口，根据角色动态开启
        btnManagement.setVisibility(View.GONE);
        if (lineFarmer != null) lineFarmer.setVisibility(View.GONE);

        switch (role) {
            case 0: // 社区居民
                tvRoleBadge.setText("🏠 社区居民");
                tvRoleBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // 绿色
                break;

            case 1: // 🌾 入驻农户 (修正：Role 1 对应农户号)
                tvRoleBadge.setText("🌾 入驻农户");
                tvRoleBadge.setBackgroundColor(Color.parseColor("#FF9800")); // 橙色
                // 显示并配置农户专属功能
                btnManagement.setText("📦 我的商品库管理");
                btnManagement.setVisibility(View.VISIBLE);
                if (lineFarmer != null) lineFarmer.setVisibility(View.VISIBLE);

                // 跳转逻辑：去农户的商品库页面
                btnManagement.setOnClickListener(v -> {
                    startActivity(new Intent(this, MyProductsActivity.class));
                });
                break;

            case 2: // 👨‍👩‍👧‍👦 社区团长 (修正：Role 2 对应团长号)
                tvRoleBadge.setText("👨‍👩‍👧‍👦 社区团长");
                tvRoleBadge.setBackgroundColor(Color.parseColor("#2196F3")); // 蓝色
                // 显示并配置团长专属功能
                btnManagement.setText("📋 社区提货点管理");
                btnManagement.setVisibility(View.VISIBLE);
                if (lineFarmer != null) lineFarmer.setVisibility(View.VISIBLE);

                // 跳转逻辑：去团长的订单核销页面
                btnManagement.setOnClickListener(v -> {
                    startActivity(new Intent(this, GroupLeaderActivity.class));
                });
                break;

            case 3: // 平台管理员
                tvRoleBadge.setText("🛡️ 平台管理员");
                tvRoleBadge.setBackgroundColor(Color.parseColor("#9C27B0")); // 紫色
                break;

            default:
                tvRoleBadge.setText("普通用户");
                break;
        }

        // 4. 通用功能监听
        btnMyOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderActivity.class));
        });

        btnMyCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        // 5. 安全退出
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "安全退出成功", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}