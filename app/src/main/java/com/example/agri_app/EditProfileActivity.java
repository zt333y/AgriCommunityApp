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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private EditText etNickname, etPassword;
    private Long userId;
    private Button btnSave;

    private static final int PICK_IMAGE_REQUEST = 1;
    private boolean isAvatarChanged = false; // 🌟 标记：用户这次进来有没有更换新头像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        ivAvatar = findViewById(R.id.iv_edit_avatar);
        etNickname = findViewById(R.id.et_nickname);
        etPassword = findViewById(R.id.et_password);
        btnSave = findViewById(R.id.btn_save_profile);

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sp.getLong("userId", -1L);
        etNickname.setText(sp.getString("nickname", sp.getString("username", "当前昵称")));

        // 加载本地缓存头像展示
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

        btnSave.setOnClickListener(v -> handleSave());
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

                Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
                if (bitmap != null) {
                    ivAvatar.setImageBitmap(bitmap);
                    isAvatarChanged = true; // 🌟 标记：用户刚才换了头像
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "读取图片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 处理点击保存的逻辑
    private void handleSave() {
        String newNickname = etNickname.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (newNickname.isEmpty()) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("正在保存...");

        // 🌟 核心逻辑：如果用户换了头像，先上传头像拿到真实URL，再保存资料
        if (isAvatarChanged) {
            File avatarFile = new File(getFilesDir(), "avatar_" + userId + ".jpg");
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), avatarFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", avatarFile.getName(), requestFile);

            RetrofitClient.getApi().uploadImage(body).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        String uploadedAvatarUrl = response.body().data;
                        // 头像上传成功，提交所有资料到后端
                        submitProfileToServer(newNickname, newPassword, uploadedAvatarUrl);
                    } else {
                        Toast.makeText(EditProfileActivity.this, "头像上传失败", Toast.LENGTH_SHORT).show();
                        resetButton();
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(EditProfileActivity.this, "网络异常，上传失败", Toast.LENGTH_SHORT).show();
                    resetButton();
                }
            });
        } else {
            // 用户没换头像，直接提交资料
            submitProfileToServer(newNickname, newPassword, null);
        }
    }

    // 真正向后端提交 User 对象的方法
    private void submitProfileToServer(String newNickname, String newPassword, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setUsername(newNickname);
        if (!newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }
        if (avatarUrl != null) {
            user.setAvatar(avatarUrl); // 🌟 将拿到的服务器图片地址赋进去
        }

        RetrofitClient.getApi().updateProfile(user).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                    editor.putString("nickname", newNickname);
                    editor.putString("username", newNickname);
                    if (!newPassword.isEmpty()) editor.putString("password", newPassword);
                    if (avatarUrl != null) editor.putString("avatar", avatarUrl); // 可选：把头像路径也缓存在本地
                    editor.apply();

                    Toast.makeText(EditProfileActivity.this, "资料修改成功！", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                    resetButton();
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private void resetButton() {
        btnSave.setEnabled(true);
        btnSave.setText("保存资料");
    }
}