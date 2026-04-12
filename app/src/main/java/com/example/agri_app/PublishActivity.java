package com.example.agri_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.math.BigDecimal;

// 🌟 核心：就是少了下面这两句！请把它们加上（注意包名如果是 entity 或者 common 根据你的实际情况稍微对一下）
import com.example.agri_app.entity.Product;
import com.example.agri_app.entity.Result;

public class PublishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        EditText etName = findViewById(R.id.et_pub_name);
        EditText etCategory = findViewById(R.id.et_pub_category);
        EditText etPrice = findViewById(R.id.et_pub_price);
        EditText etStock = findViewById(R.id.et_pub_stock);
        EditText etUnit = findViewById(R.id.et_pub_unit);
        EditText etImage = findViewById(R.id.et_pub_image);
        EditText etDesc = findViewById(R.id.et_pub_desc);
        Button btnPublish = findViewById(R.id.btn_publish);

        btnPublish.setOnClickListener(v -> {
            // 1. 抓取输入框的值
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();

            // 2. 简单防空校验
            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "必填项不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🌟 3. 新增：从缓存中取出当前登录的农户 ID（默认给3是为了防呆，3是数据库里李大爷的ID）
            android.content.SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long userId = sp.getLong("userId", 3L);

            // 4. 组装要发送给后端的 Product 对象
            Product newProduct = new Product();
            newProduct.setFarmerId(userId); // 🌟🌟🌟 核心修复：把农户 ID 塞进去！
            newProduct.setName(name);
            newProduct.setCategory(etCategory.getText().toString().trim());
            newProduct.setPrice(Double.parseDouble(priceStr));
            newProduct.setStock(Integer.parseInt(stockStr));
            newProduct.setUnit(etUnit.getText().toString().trim());
            newProduct.setImageUrl(etImage.getText().toString().trim());
            newProduct.setDescription(etDesc.getText().toString().trim());

            // 5. 发起网络请求
            RetrofitClient.getApi().addProduct(newProduct).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(PublishActivity.this, "🎉 发布成功，请等待审核", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        // 🌟 修改：如果失败，把后端传回来的真正死因（msg）打印在屏幕上
                        String errorMsg = response.body() != null ? response.body().msg : "未知状态";
                        Toast.makeText(PublishActivity.this, "发布失败: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(PublishActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}