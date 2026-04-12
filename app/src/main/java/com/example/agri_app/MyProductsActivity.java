package com.example.agri_app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agri_app.adapter.MyProductAdapter;
import com.example.agri_app.entity.Product;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProductsActivity extends AppCompatActivity {
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products); // 记得创建一个带RecyclerView的简单布局
        setTitle("我的商品库管理");

        rv = findViewById(R.id.rv_my_products);
        rv.setLayoutManager(new LinearLayoutManager(this));

        loadMyProducts();
    }

    private void loadMyProducts() {
        RetrofitClient.getApi().getMyProducts().enqueue(new Callback<Result<List<Product>>>() {
            @Override
            public void onResponse(Call<Result<List<Product>>> call, Response<Result<List<Product>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    rv.setAdapter(new MyProductAdapter(response.body().data));
                }
            }
            @Override
            public void onFailure(Call<Result<List<Product>>> call, Throwable t) {
                Toast.makeText(MyProductsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}