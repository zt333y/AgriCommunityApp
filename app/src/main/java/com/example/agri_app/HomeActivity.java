package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

        // ================= 下面是核心模块入口 =================

        // 3. 去购物车
        findViewById(R.id.btn_go_cart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        // 4. 去我的订单
        findViewById(R.id.btn_go_order).setOnClickListener(v -> {
            startActivity(new Intent(this, OrderActivity.class));
        });

        // 5. 去个人中心
        findViewById(R.id.btn_go_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // 🌟 6. 新增：跳转至【发布农产品】页面
        Button btnGoPublish = findViewById(R.id.btn_go_publish);

        // 🌟 核心修改：统一使用 "UserPrefs" 确保能读取到 MainActivity 存入的数据 [cite: 45, 198]
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // 获取角色信息，默认为 0 (居民) [cite: 228]
        int role = sp.getInt("role", 0);

        // 根据角色判断显隐：role == 1 代表农户
        if (role == 1) {
            btnGoPublish.setVisibility(View.VISIBLE); // 农户可见 [cite: 118]
        } else {
            btnGoPublish.setVisibility(View.GONE);    // 其他角色隐藏
        }

        btnGoPublish.setOnClickListener(v -> {
            startActivity(new Intent(this, PublishActivity.class));
        });
    }

    private void loadProducts(String keyword) {
        RetrofitClient.getApi().getProductList(keyword).enqueue(new Callback<Result<List<Product>>>() {
            @Override
            public void onResponse(Call<Result<List<Product>>> call, Response<Result<List<Product>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    recyclerView.setAdapter(new ProductAdapter(response.body().data));
                } else {
                    Toast.makeText(HomeActivity.this, "加载失败: " + (response.body() != null ? response.body().msg : "接口不存在"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<List<Product>>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}