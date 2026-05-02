package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private EditText etNickname, etPassword;
    private Long userId;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        ivAvatar = findViewById(R.id.iv_edit_avatar);
        etNickname = findViewById(R.id.et_nickname);
        etPassword = findViewById(R.id.et_password);
        Button btnSave = findViewById(R.id.btn_save_profile);

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sp.getLong("userId", -1L);
        etNickname.setText(sp.getString("nickname", sp.getString("username", "当前昵称")));

        File avatarFile = new File(getFilesDir(), "avatar_" + userId + ".jpg");
        if (avatarFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
            if (bitmap != null) {
                ivAvatar.setImageBitmap(bitmap);
            }
        }

        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> saveProfile());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream == null) return;

                File avatarFile = new File(getFilesDir(), "avatar_" + userId + ".jpg");
                OutputStream outputStream = new FileOutputStream(avatarFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();

                // 🌟 核心修复：复制完成后，立即用 BitmapFactory 解码并设置，绝不使用 setImageURI
                Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
                if (bitmap != null) {
                    ivAvatar.setImageBitmap(bitmap);
                }

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
                    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                    editor.putString("nickname", newNickname);
                    editor.putString("username", newNickname);
                    if (!newPassword.isEmpty()) editor.putString("password", newPassword);
                    editor.apply();

                    Toast.makeText(EditProfileActivity.this, "资料修改成功！", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
    }
}