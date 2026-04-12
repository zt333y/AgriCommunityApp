package com.example.agri_app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agri_app.adapter.GroupLeaderOrderAdapter;
import com.example.agri_app.entity.OrderVO;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupLeaderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_leader);

        recyclerView = findViewById(R.id.rv_leader_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadCommunityOrders();
    }

    private void loadCommunityOrders() {
        RetrofitClient.getApi().getLeaderOrders().enqueue(new Callback<Result<List<OrderVO>>>() {
            @Override
            public void onResponse(Call<Result<List<OrderVO>>> call, Response<Result<List<OrderVO>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    recyclerView.setAdapter(new GroupLeaderOrderAdapter(response.body().data));
                }
            }
            @Override
            public void onFailure(Call<Result<List<OrderVO>>> call, Throwable t) {
                Toast.makeText(GroupLeaderActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
    }
}