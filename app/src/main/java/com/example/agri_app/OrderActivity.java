package com.example.agri_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

    private RecyclerView rvOrder;
    private Long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        rvOrder = findViewById(R.id.rv_order);
        rvOrder.setLayoutManager(new LinearLayoutManager(this));

        // 1. 获取当前登录用户的 ID
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = sp.getLong("userId", 1L);

        // 2. 去后端拉取订单数据
        loadOrderData();
    }

    private void loadOrderData() {
        RetrofitClient.getApi().getOrderList(currentUserId).enqueue(new Callback<Result<List<OrderVO>>>() {
            @Override
            public void onResponse(Call<Result<List<OrderVO>>> call, Response<Result<List<OrderVO>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    List<OrderVO> list = response.body().data;

                    View layoutEmpty = findViewById(R.id.layout_empty_order);

                    // 如果没数据，显示空状态
                    if (list == null || list.isEmpty()) {
                        rvOrder.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        // 有数据，显示列表
                        rvOrder.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                        rvOrder.setAdapter(new OrderAdapter(list));
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<List<OrderVO>>> call, Throwable t) {
                Toast.makeText(OrderActivity.this, "加载订单失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}