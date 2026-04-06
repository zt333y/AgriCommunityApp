package com.example.agri_app;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agri_app.adapter.ProductAdapter;
import com.example.agri_app.entity.Product;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 1. 刚进页面时，加载所有商品
        loadProducts("");

        // 2. 顶部搜索功能
        EditText etSearch = findViewById(R.id.et_search);
        findViewById(R.id.btn_search).setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            loadProducts(keyword);
        });

        // ================= 下面是找回来的三大金刚键 =================

        // 3. 去购物车
        findViewById(R.id.btn_go_cart).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, CartActivity.class));
        });

        // 4. 去我的订单
        findViewById(R.id.btn_go_order).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, OrderActivity.class));
        });

        // 5. 去个人中心
        findViewById(R.id.btn_go_profile).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ProfileActivity.class));
        });
    }

    private void loadProducts(String keyword) {
        // 请求网络时带上搜索词
        RetrofitClient.getApi().getProductList(keyword).enqueue(new Callback<Result<List<Product>>>() {
            @Override
            public void onResponse(Call<Result<List<Product>>> call, Response<Result<List<Product>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    recyclerView.setAdapter(new ProductAdapter(response.body().data));
                }
            }
            @Override
            public void onFailure(Call<Result<List<Product>>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}