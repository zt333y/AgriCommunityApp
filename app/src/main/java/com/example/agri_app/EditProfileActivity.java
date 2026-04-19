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
    private String selectedAvatarUri = "";

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ivAvatar = findViewById(R.id.iv_edit_avatar);
        etNickname = findViewById(R.id.et_nickname);
        etPassword = findViewById(R.id.et_password);
        Button btnSave = findViewById(R.id.btn_save_profile);

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sp.getLong("userId", -1L);
        // 回显昵称
        etNickname.setText(sp.getString("nickname", sp.getString("username", "当前昵称")));

        // 尝试加载本地头像
        selectedAvatarUri = sp.getString("avatarUri", "");
        if (!selectedAvatarUri.isEmpty()) {
            try {
                ivAvatar.setImageURI(Uri.parse(selectedAvatarUri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 点击头像
        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // 点击保存
        btnSave.setOnClickListener(v -> saveProfile());
    }

    // 🌟 接管相册图片：直接复制一份到我们自己 App 的沙盒目录，彻底解决小米权限闪退问题！
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream == null) return;

                java.io.File avatarFile = new java.io.File(getFilesDir(), "my_avatar.jpg");
                java.io.OutputStream outputStream = new java.io.FileOutputStream(avatarFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();

                // 将新生成的、完全受控制的文件路径存起来
                selectedAvatarUri = Uri.fromFile(avatarFile).toString();
                ivAvatar.setImageURI(Uri.parse(selectedAvatarUri));

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "读取图片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfile() {
        String newNickname = etNickname.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (newNickname.isEmpty()) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🌟 核心：统一使用 setUsername！绝不使用 setRealName
        User user = new User();
        user.setId(userId);
        user.setUsername(newNickname);
        if (!newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }

        RetrofitClient.getApi().updateProfile(user).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    // 同步更新本地缓存
                    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                    editor.putString("nickname", newNickname);
                    editor.putString("username", newNickname); // 顺便把旧的 username 缓存也改掉
                    editor.putString("avatarUri", selectedAvatarUri);
                    if (!newPassword.isEmpty()) editor.putString("password", newPassword);
                    editor.apply();

                    Toast.makeText(EditProfileActivity.this, "资料修改成功！", Toast.LENGTH_SHORT).show();
                    finish();
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