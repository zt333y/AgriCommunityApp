package com.example.agri_app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agri_app.adapter.OrderAdapter;
import com.example.agri_app.entity.OrderVO;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        RecyclerView recyclerView = findViewById(R.id.order_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 拉取订单数据
        RetrofitClient.getApi().getOrderList(1L).enqueue(new Callback<Result<List<OrderVO>>>() {
            @Override
            public void onResponse(Call<Result<List<OrderVO>>> call, Response<Result<List<OrderVO>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    recyclerView.setAdapter(new OrderAdapter(response.body().data));
                }
            }
            @Override
            public void onFailure(Call<Result<List<OrderVO>>> call, Throwable t) {
                Toast.makeText(OrderActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}