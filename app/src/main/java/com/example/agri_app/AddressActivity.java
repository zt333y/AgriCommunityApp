package com.example.agri_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressActivity extends AppCompatActivity {

    private EditText etAddress;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        etAddress = findViewById(R.id.et_address);
        Button btnSave = findViewById(R.id.btn_save_address);

        // 1. 从缓存获取当前用户ID和旧地址（回显）
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sp.getLong("userId", -1L);
        String currentAddr = sp.getString("address", "");
        etAddress.setText(currentAddr);

        // 2. 点击保存
        btnSave.setOnClickListener(v -> {
            String newAddress = etAddress.getText().toString().trim();
            if (newAddress.isEmpty()) {
                Toast.makeText(this, "地址不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            saveAddressToBackend(newAddress);
        });
    }

    private void saveAddressToBackend(String address) {
        // 调用 ApiService 中的接口
        RetrofitClient.getApi().updateAddress(userId, address).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    // 同步更新本地缓存，这样购物车结算时能直接拿
                    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                    editor.putString("address", address);
                    editor.apply();

                    Toast.makeText(AddressActivity.this, "地址保存成功！", Toast.LENGTH_SHORT).show();
                    finish(); // 返回个人中心
                } else {
                    Toast.makeText(AddressActivity.this, "保存失败，请稍后再试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Toast.makeText(AddressActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
    }
}