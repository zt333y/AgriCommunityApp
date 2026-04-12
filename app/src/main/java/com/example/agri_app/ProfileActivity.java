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

        // 农户专属入口（对应我们之前在布局中新增的 ID）
        TextView btnMyProducts = findViewById(R.id.btn_my_products);
        View lineFarmer = findViewById(R.id.line_farmer);

        Button btnLogout = findViewById(R.id.btn_logout);

        // 2. 从本地缓存 UserPrefs 中抓取真实登录信息
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sp.getString("username", "农产品用户");
        int role = sp.getInt("role", 0); // 0:居民, 1:农户, 2:团长, 3:管理员

        // 3. 动态渲染用户信息与权限标签
        tvUsername.setText(username);

        // 默认隐藏农户专用入口，只有角色匹配才开启
        btnMyProducts.setVisibility(View.GONE);
        if (lineFarmer != null) lineFarmer.setVisibility(View.GONE);

        switch (role) {
            case 0:
                tvRoleBadge.setText("🏠 社区居民");
                tvRoleBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // 绿色
                break;
            case 1:
                tvRoleBadge.setText("🌾 入驻农户");
                tvRoleBadge.setBackgroundColor(Color.parseColor("#FF9800")); // 橙色
                // 🌟 核心：如果是农户，展示商品库管理入口
                btnMyProducts.setVisibility(View.VISIBLE);
                if (lineFarmer != null) lineFarmer.setVisibility(View.VISIBLE);
                break;
            case 2:
                tvRoleBadge.setText("👨‍👩‍👧‍👦 社区团长");
                tvRoleBadge.setBackgroundColor(Color.parseColor("#2196F3")); // 蓝色
                break;
            case 3:
                tvRoleBadge.setText("🛡️ 平台管理员");
                tvRoleBadge.setBackgroundColor(Color.parseColor("#9C27B0")); // 紫色
                break;
            default:
                tvRoleBadge.setText("普通用户");
                break;
        }

        // 4. 设置各项功能跳转监听

        // 跳转：我的订单
        btnMyOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderActivity.class));
        });

        // 跳转：我的购物车
        btnMyCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        // 跳转：我的商品库 (仅农户可见并点击)
        btnMyProducts.setOnClickListener(v -> {
            startActivity(new Intent(this, MyProductsActivity.class));
        });

        // 5. 🌟 安全退出逻辑
        btnLogout.setOnClickListener(v -> {
            // 清空本地缓存的所有用户信息 (token, userId, role 等)
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "安全退出成功", Toast.LENGTH_SHORT).show();

            // 跳转回登录页，并清空 Activity 任务栈，防止按返回键退回
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}