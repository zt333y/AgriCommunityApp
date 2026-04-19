package com.example.agri_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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
        setTitle("资质入驻申请");

        // 🌟 1. 严格根据你的 activity_apply.xml 里的 ID 进行绑定
        RadioGroup rgRole = findViewById(R.id.rg_role);
        EditText etName = findViewById(R.id.et_real_name);
        EditText etIdCard = findViewById(R.id.et_id_card);
        EditText etAddress = findViewById(R.id.et_apply_address);
        Button btnSubmit = findViewById(R.id.btn_submit_apply);

        btnSubmit.setOnClickListener(v -> {
            String nameStr = etName.getText().toString().trim();
            String idCardStr = etIdCard.getText().toString().trim();
            String addressStr = etAddress.getText().toString().trim();

            if (nameStr.isEmpty() || idCardStr.isEmpty() || addressStr.isEmpty()) {
                Toast.makeText(this, "请填写完整的申请信息", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long currentUserId = sp.getLong("userId", 0L);
            if (currentUserId == 0L) return;

            // 🌟 2. 组装实体对象，严格匹配你的 Apply.java 中的属性名
            Apply apply = new Apply();
            apply.setUserId(currentUserId);
            apply.setRealName(nameStr);
            apply.setIdCard(idCardStr);
            apply.setAddress(addressStr);

            // 判定选中的是哪一个单选框: rb_farmer(农户) -> role 1, rb_leader(团长) -> role 2
            int selectedRole = (rgRole.getCheckedRadioButtonId() == R.id.rb_farmer) ? 1 : 2;
            apply.setApplyRole(selectedRole);

            // 锁定按钮，防连点
            btnSubmit.setText("正在提交...");
            btnSubmit.setEnabled(false);

            // 🌟 3. 发起网络请求提交给后端
            RetrofitClient.getApi().submitApply(apply).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(ApplyActivity.this, "🎉 申请已提交，请等待管理员审核", Toast.LENGTH_LONG).show();
                        finish(); // 提交成功后关闭页面
                    } else {
                        Toast.makeText(ApplyActivity.this, "提交失败: " + (response.body() != null ? response.body().msg : ""), Toast.LENGTH_SHORT).show();
                        btnSubmit.setText("提交入驻申请");
                        btnSubmit.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(ApplyActivity.this, "网络异常，请检查网络", Toast.LENGTH_SHORT).show();
                    btnSubmit.setText("提交入驻申请");
                    btnSubmit.setEnabled(true);
                }
            });
        });
    }
}