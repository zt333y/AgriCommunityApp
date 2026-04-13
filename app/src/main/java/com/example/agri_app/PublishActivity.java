package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.entity.Product;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        // 绑定控件
        EditText etName = findViewById(R.id.et_pub_name);
        EditText etCategory = findViewById(R.id.et_pub_category);
        EditText etPrice = findViewById(R.id.et_pub_price);
        EditText etStock = findViewById(R.id.et_pub_stock);
        EditText etUnit = findViewById(R.id.et_pub_unit);
        EditText etImage = findViewById(R.id.et_pub_image);
        EditText etDesc = findViewById(R.id.et_pub_desc);
        Button btnPublish = findViewById(R.id.btn_publish);

        btnPublish.setOnClickListener(v -> {
            // 1. 获取输入值
            String name = etName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();
            String unit = etUnit.getText().toString().trim();
            String imageUrl = etImage.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            // 2. 严格的防空校验
            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "商品名称、分类、价格和库存为必填项", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. 获取当前登录的农户 ID
            SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long farmerId = sp.getLong("userId", 0L); // 默认 0，后端如果收到 0 应该报错

            if (farmerId == 0) {
                Toast.makeText(this, "未检测到有效登录状态，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }

            // 4. 组装实体对象
            Product newProduct = new Product();
            newProduct.setFarmerId(farmerId);
            newProduct.setName(name);
            newProduct.setCategory(category);
            newProduct.setUnit(unit);
            newProduct.setImageUrl(imageUrl.isEmpty() ? "https://img14.360buyimg.com/n0/jfs/t1/133282/22/16543/123165/5f9780a4Eeb76451a/10f215dff7677db0.jpg" : imageUrl);
            newProduct.setDescription(desc);

            try {
                newProduct.setPrice(Double.parseDouble(priceStr));
                newProduct.setStock(Integer.parseInt(stockStr));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "价格或库存格式输入错误", Toast.LENGTH_SHORT).show();
                return;
            }

            // 5. 提交至后端审核
            RetrofitClient.getApi().addProduct(newProduct).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(PublishActivity.this, "🎉 发布成功！已提交平台审核", Toast.LENGTH_LONG).show();

                        // 自动跳转到“我的商品库”页面
                        startActivity(new Intent(PublishActivity.this, MyProductsActivity.class));
                        finish();
                    } else {
                        String errorMsg = response.body() != null ? response.body().msg : "未知错误";
                        Toast.makeText(PublishActivity.this, "发布失败: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(PublishActivity.this, "网络错误: 请检查服务器是否启动", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}