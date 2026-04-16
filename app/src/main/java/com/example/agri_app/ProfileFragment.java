package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        // 1. 绑定基础控件
        TextView tvUsername = view.findViewById(R.id.tv_username);
        TextView tvRoleBadge = view.findViewById(R.id.tv_role_badge);
        TextView btnOrders = view.findViewById(R.id.btn_my_orders);
        TextView btnMyAddress = view.findViewById(R.id.btn_my_address);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        // 2. 绑定带有特殊角色权限的控件及其分割线
        TextView btnManagement = view.findViewById(R.id.btn_my_products); // 管理功能入口
        View lineFarmer = view.findViewById(R.id.line_farmer);           // 管理入口上方的分割线

        TextView btnPicking = view.findViewById(R.id.btn_picking_summary); // 采摘汇总入口
        View linePicking = view.findViewById(R.id.line_picking);           // 采摘汇总上方的分割线

        // 3. 读取用户信息
        SharedPreferences sp = getContext().getSharedPreferences("UserPrefs", 0);
        int role = sp.getInt("role", 0);
        tvUsername.setText(sp.getString("username", "用户"));

        // 4. 根据角色动态配置 UI (默认隐藏专属功能)
        btnPicking.setVisibility(View.GONE);
        btnManagement.setVisibility(View.GONE);

        // 🌟 核心修复：确保默认状态下，这些动态分割线也是隐藏的，防止UI乱掉
        if (lineFarmer != null) lineFarmer.setVisibility(View.GONE);
        if (linePicking != null) linePicking.setVisibility(View.GONE);

        if (role == 1) { // 🌾 【如果是农户】
            tvRoleBadge.setText("🌾 入驻农户");
            tvRoleBadge.setBackgroundColor(Color.parseColor("#FF9800"));

            // 显示农户专属的两个按钮和对应的分割线
            btnManagement.setVisibility(View.VISIBLE);
            btnManagement.setText("🌽 发布农产品 / 我的商品库");
            if (lineFarmer != null) lineFarmer.setVisibility(View.VISIBLE);

            btnPicking.setVisibility(View.VISIBLE);
            if (linePicking != null) linePicking.setVisibility(View.VISIBLE);

            // 绑定跳转事件
            btnManagement.setOnClickListener(v -> startActivity(new Intent(getContext(), MyProductsActivity.class)));
            btnPicking.setOnClickListener(v -> startActivity(new Intent(getContext(), PickingListActivity.class)));

        } else if (role == 2) { // 👨‍👩‍👧‍👦 【如果是团长】
            tvRoleBadge.setText("👨‍👩‍👧‍👦 社区团长");
            tvRoleBadge.setBackgroundColor(Color.parseColor("#2196F3"));

            // 团长只显示一个管理按钮
            btnManagement.setVisibility(View.VISIBLE);
            btnManagement.setText("📋 社区提货点管理");
            if (lineFarmer != null) lineFarmer.setVisibility(View.VISIBLE);

            // 绑定跳转事件
            btnManagement.setOnClickListener(v -> startActivity(new Intent(getContext(), GroupLeaderActivity.class)));

        } else { // 🏠 【普通居民】
            tvRoleBadge.setText("🏠 社区居民");
            tvRoleBadge.setBackgroundColor(Color.parseColor("#4CAF50"));
        }

        // ================== 通用按钮跳转 ==================
        if (btnOrders != null) {
            btnOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), OrderActivity.class)));
        }
        if (btnMyAddress != null) {
            btnMyAddress.setOnClickListener(v -> startActivity(new Intent(getContext(), AddressActivity.class)));
        }

        // 5. 安全退出
        btnLogout.setOnClickListener(v -> {
            sp.edit().clear().apply();
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}