package com.example.agri_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.entity.Apply;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        // 1. 初始化控件
        EditText etRealName = findViewById(R.id.et_real_name);
        EditText etIdCard = findViewById(R.id.et_id_card);
        EditText etAddress = findViewById(R.id.et_apply_address);
        RadioButton rbFarmer = findViewById(R.id.rb_farmer);
        Button btnSubmit = findViewById(R.id.btn_submit_apply);

        // 2. 提交按钮点击事件
        btnSubmit.setOnClickListener(v -> {
            // 抓取输入内容
            String realName = etRealName.getText().toString().trim();
            String idCard = etIdCard.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            // 简单校验
            if (realName.isEmpty() || idCard.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "请填写完整的申请信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. 组装申请对象
            Apply a = new Apply();
            // 从本地缓存获取当前登录用户的 ID
            long userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getLong("userId", 0);
            a.setUserId(userId);

            // 设置申请角色：1为农户，2为团长
            a.setApplyRole(rbFarmer.isChecked() ? 1 : 2);
            a.setRealName(realName);
            a.setIdCard(idCard);
            a.setAddress(address);

            // 4. 发起网络请求
            RetrofitClient.getApi().submitApply(a).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(ApplyActivity.this, "🎉 申请提交成功，请等待管理员审核", Toast.LENGTH_LONG).show();
                        finish(); // 提交成功后关闭页面
                    } else {
                        String msg = response.body() != null ? response.body().msg : "提交失败";
                        Toast.makeText(ApplyActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(ApplyActivity.this, "网络异常: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}