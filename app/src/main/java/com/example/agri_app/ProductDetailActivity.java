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
            // 假设当前登录的用户ID是 1 (系统管理员admin)，固定数量为 1 份
            com.example.agri_app.entity.Cart cart = new com.example.agri_app.entity.Cart(1L, productId, 1);

            // 发起网络请求
            com.example.agri_app.network.RetrofitClient.getApi().addCart(cart).enqueue(new retrofit2.Callback<com.example.agri_app.entity.Result<String>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.agri_app.entity.Result<String>> call, retrofit2.Response<com.example.agri_app.entity.Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(ProductDetailActivity.this, "太棒了！已存入数据库购物车！", Toast.LENGTH_LONG).show();
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