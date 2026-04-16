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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. 搬迁布局
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        // 2. 找到控件
        TextView tvUsername = view.findViewById(R.id.tv_username);
        TextView tvRoleBadge = view.findViewById(R.id.tv_role_badge);
        TextView btnOrders = view.findViewById(R.id.btn_my_orders);
        TextView btnManagement = view.findViewById(R.id.btn_my_products);
        TextView btnPicking = view.findViewById(R.id.btn_picking_summary);

        // 🌟 新增：绑定“我的收货地址”按钮
        TextView btnMyAddress = view.findViewById(R.id.btn_my_address);

        Button btnLogout = view.findViewById(R.id.btn_logout);

        SharedPreferences sp = getContext().getSharedPreferences("UserPrefs", 0);
        int role = sp.getInt("role", 0);
        tvUsername.setText(sp.getString("username", "用户"));

        // 3. 根据角色配置 (逻辑搬迁)
        btnPicking.setVisibility(View.GONE);
        btnManagement.setVisibility(View.GONE);

        if (role == 1) { // 农户
            tvRoleBadge.setText("🌾 入驻农户");
            tvRoleBadge.setBackgroundColor(Color.parseColor("#FF9800"));
            btnManagement.setVisibility(View.VISIBLE);
            btnPicking.setVisibility(View.VISIBLE);
            btnManagement.setOnClickListener(v -> startActivity(new Intent(getContext(), MyProductsActivity.class)));
            btnPicking.setOnClickListener(v -> startActivity(new Intent(getContext(), PickingListActivity.class)));
        } else if (role == 2) { // 团长
            tvRoleBadge.setText("👨‍👩‍👧‍👦 社区团长");
            tvRoleBadge.setBackgroundColor(Color.parseColor("#2196F3"));
            btnManagement.setText("📋 社区提货点管理");
            btnManagement.setVisibility(View.VISIBLE);
            btnManagement.setOnClickListener(v -> startActivity(new Intent(getContext(), GroupLeaderActivity.class)));
        }

        // 通用功能点击事件
        btnOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), OrderActivity.class)));

        // 🌟 新增：跳转到收货地址编辑页面
        if (btnMyAddress != null) {
            btnMyAddress.setOnClickListener(v -> startActivity(new Intent(getContext(), AddressActivity.class)));
        }

        // 退出登录逻辑
        btnLogout.setOnClickListener(v -> {
            sp.edit().clear().apply();
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}