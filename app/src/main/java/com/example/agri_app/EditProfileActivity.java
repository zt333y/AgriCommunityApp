package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.entity.Result;
import com.example.agri_app.entity.User;
import com.example.agri_app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private EditText etNickname, etPassword;
    private Long userId;
    private String selectedAvatarUri = ""; // 用于保存本地头像路径

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ivAvatar = findViewById(R.id.iv_edit_avatar);
        etNickname = findViewById(R.id.et_nickname);
        etPassword = findViewById(R.id.et_password);
        Button btnSave = findViewById(R.id.btn_save_profile);

        // 1. 获取本地缓存的用户数据回显
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sp.getLong("userId", -1L);
        // 回显昵称（如果没有设置过，默认显示当前用户名）
        etNickname.setText(sp.getString("nickname", sp.getString("username", "当前昵称")));

        // 2. 🌟 核心修复：尝试加载本地头像（修复了之前的报错变量名）
        selectedAvatarUri = sp.getString("avatarUri", "");
        if (!selectedAvatarUri.isEmpty()) {
            ivAvatar.setImageURI(Uri.parse(selectedAvatarUri));
        }

        // 3. 点击头像：调用系统原生相册
        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // 4. 点击保存
        btnSave.setOnClickListener(v -> saveProfile());
    }

    // 接收相册选择的图片
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            selectedAvatarUri = imageUri.toString();
            ivAvatar.setImageURI(imageUri); // 界面立刻更新
        }
    }

    private void saveProfile() {
        String newNickname = etNickname.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (newNickname.isEmpty()) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 封装请求数据 (使用 realName 承载昵称)
        User user = new User();
        user.setId(userId);
        user.setRealName(newNickname);
        if (!newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }

        // 发送网络请求给后端
        RetrofitClient.getApi().updateProfile(user).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    // 保存成功后，同步更新本地的 SP 缓存
                    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                    editor.putString("nickname", newNickname);
                    editor.putString("avatarUri", selectedAvatarUri); // 把头像路径存本地
                    if (!newPassword.isEmpty()) editor.putString("password", newPassword);
                    editor.apply();

                    Toast.makeText(EditProfileActivity.this, "资料修改成功！", Toast.LENGTH_SHORT).show();
                    finish(); // 返回个人中心
                } else {
                    Toast.makeText(EditProfileActivity.this, "修改失败，后端报错", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
    }
}