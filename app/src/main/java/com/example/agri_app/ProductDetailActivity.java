package com.example.agri_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // 1. 找到界面上的控件
        TextView tvName = findViewById(R.id.detail_name);
        TextView tvPrice = findViewById(R.id.detail_price);
        TextView tvDesc = findViewById(R.id.detail_desc);
        Button btnAddCart = findViewById(R.id.btn_add_cart);

        // 2. 接收从上一个页面（列表页）传过来的数据
        String name = getIntent().getStringExtra("NAME");
        double price = getIntent().getDoubleExtra("PRICE", 0.0);
        String desc = getIntent().getStringExtra("DESC");
        String imageUrl = getIntent().getStringExtra("IMAGE_URL");
        com.bumptech.glide.Glide.with(this)
                .load(imageUrl)
                .into((android.widget.ImageView) findViewById(R.id.detail_image));
        // 3. 把数据展示在界面上
        tvName.setText(name);
        tvPrice.setText("￥" + price);
        tvDesc.setText(desc);

        // 获取刚才列表传过来的真实商品ID
        long productId = getIntent().getLongExtra("ID", 0);

// 4. 给加入购物车按钮设置真实的点击事件！
        btnAddCart.setOnClickListener(v -> {
            // 🌟 核心修复：从本地缓存提取真实登录用户的 ID
            android.content.SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            // 尝试获取保存的 userId，如果没取到（没登录），默认返回 -1
            long currentUserId = sp.getLong("userId", -1L);

            if (currentUserId == -1L) {
                Toast.makeText(ProductDetailActivity.this, "请先登录！", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🌟 核心修复：把获取到的真实 currentUserId 放进购物车对象里
            com.example.agri_app.entity.Cart cart = new com.example.agri_app.entity.Cart(currentUserId, productId, 1);

            // 发起网络请求
            com.example.agri_app.network.RetrofitClient.getApi().addCart(cart).enqueue(new retrofit2.Callback<com.example.agri_app.entity.Result<String>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.agri_app.entity.Result<String>> call, retrofit2.Response<com.example.agri_app.entity.Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(ProductDetailActivity.this, "太棒了！已加入您的购物车！", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "加入失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.agri_app.entity.Result<String>> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}