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

            // 3. 组装要发送给后端的 Product 对象 (注意：确保你的前端 Product 实体类里有这些字段的 set 方法)
            Product newProduct = new Product();
            newProduct.setName(name);
            newProduct.setCategory(etCategory.getText().toString().trim());
            newProduct.setPrice(Double.parseDouble(priceStr)); // 🌟 换成转换成 Double
            newProduct.setStock(Integer.parseInt(stockStr));
            newProduct.setUnit(etUnit.getText().toString().trim());
            newProduct.setImageUrl(etImage.getText().toString().trim());
            newProduct.setDescription(etDesc.getText().toString().trim());

            // 4. 发起网络请求
            RetrofitClient.getApi().addProduct(newProduct).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(PublishActivity.this, "🎉 " + response.body().msg, Toast.LENGTH_LONG).show();
                        finish(); // 发布成功，关闭页面
                    } else {
                        Toast.makeText(PublishActivity.this, "发布失败", Toast.LENGTH_SHORT).show();
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