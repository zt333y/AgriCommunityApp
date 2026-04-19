package com.example.agri_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvUserId, tvRoleBadge;
    private ImageView ivAvatar;
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_profile, container, false);

        tvUsername = root.findViewById(R.id.tv_username);
        tvUserId = root.findViewById(R.id.tv_user_id);
        tvRoleBadge = root.findViewById(R.id.tv_role_badge);
        ivAvatar = root.findViewById(R.id.iv_avatar);

        TextView btnEditProfile = root.findViewById(R.id.btn_edit_profile);
        TextView btnOrders = root.findViewById(R.id.btn_my_orders);
        TextView btnMyAddress = root.findViewById(R.id.btn_my_address);
        Button btnLogout = root.findViewById(R.id.btn_logout);

        // 🌟 点击那排按钮中的“修改个人资料”进行跳转
        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));

        TextView btnApplyRole = root.findViewById(R.id.btn_apply_role);
        View lineApply = root.findViewById(R.id.line_apply);
        TextView btnManagement = root.findViewById(R.id.btn_my_products);
        View lineFarmer = root.findViewById(R.id.line_farmer);
        TextView btnPicking = root.findViewById(R.id.btn_picking_summary);
        View linePicking = root.findViewById(R.id.line_picking);

        SharedPreferences sp = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int role = sp.getInt("role", 0);

        btnPicking.setVisibility(View.GONE);
        btnManagement.setVisibility(View.GONE);
        if (lineFarmer != null) lineFarmer.setVisibility(View.GONE);
        if (linePicking != null) linePicking.setVisibility(View.GONE);
        if (btnApplyRole != null) btnApplyRole.setVisibility(View.GONE);
        if (lineApply != null) lineApply.setVisibility(View.GONE);

        if (role == 1) {
            tvRoleBadge.setText("🌾 入驻农户");
            tvRoleBadge.setBackgroundColor(Color.parseColor("#FF9800"));
            btnManagement.setVisibility(View.VISIBLE);
            btnManagement.setText("🌽 发布农产品 / 我的商品库");
            if (lineFarmer != null) lineFarmer.setVisibility(View.VISIBLE);
            btnPicking.setVisibility(View.VISIBLE);
            if (linePicking != null) linePicking.setVisibility(View.VISIBLE);
            btnManagement.setOnClickListener(v -> startActivity(new Intent(getContext(), MyProductsActivity.class)));
            btnPicking.setOnClickListener(v -> startActivity(new Intent(getContext(), PickingListActivity.class)));
        } else if (role == 2) {
            tvRoleBadge.setText("👨‍👩‍👧‍👦 社区团长");
            tvRoleBadge.setBackgroundColor(Color.parseColor("#2196F3"));
            btnManagement.setVisibility(View.VISIBLE);
            btnManagement.setText("📋 社区提货点管理");
            if (lineFarmer != null) lineFarmer.setVisibility(View.VISIBLE);
            btnManagement.setOnClickListener(v -> startActivity(new Intent(getContext(), GroupLeaderActivity.class)));
        } else {
            tvRoleBadge.setText("🏠 社区居民");
            tvRoleBadge.setBackgroundColor(Color.parseColor("#4CAF50"));
            if (btnApplyRole != null) {
                btnApplyRole.setVisibility(View.VISIBLE);
                btnApplyRole.setOnClickListener(v -> startActivity(new Intent(getContext(), ApplyActivity.class)));
            }
            if (lineApply != null) lineApply.setVisibility(View.VISIBLE);
        }

        if (btnOrders != null) btnOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), OrderActivity.class)));
        if (btnMyAddress != null) btnMyAddress.setOnClickListener(v -> startActivity(new Intent(getContext(), AddressActivity.class)));

        btnLogout.setOnClickListener(v -> {
            sp.edit().clear().apply();
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            SharedPreferences sp = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

            // 刷新ID
            long userId = sp.getLong("userId", 0);
            tvUserId.setText("社区ID: " + userId);

            // 刷新昵称
            String nickname = sp.getString("nickname", sp.getString("username", "未登录"));
            tvUsername.setText(nickname);

            // 🌟 核心修复：加上 try-catch 防止相册权限过期导致 App 闪退
            String avatarUri = sp.getString("avatarUri", "");
            if (!avatarUri.isEmpty()) {
                try {
                    ivAvatar.setImageURI(Uri.parse(avatarUri));
                } catch (Exception e) {
                    // 如果系统收回了图片权限，就不显示它，保证 App 绝对不会崩溃
                    e.printStackTrace();
                }
            }
        }
    }
}