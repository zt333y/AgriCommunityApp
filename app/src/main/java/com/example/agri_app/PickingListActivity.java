package com.example.agri_app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agri_app.adapter.PickingListAdapter;
import com.example.agri_app.entity.FarmerPickingVO;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickingListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picking_list);

        recyclerView = findViewById(R.id.rv_picking_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadPickingData();
    }

    private void loadPickingData() {
        RetrofitClient.getApi().getPickingList().enqueue(new Callback<Result<List<FarmerPickingVO>>>() {
            @Override
            public void onResponse(Call<Result<List<FarmerPickingVO>>> call, Response<Result<List<FarmerPickingVO>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    List<FarmerPickingVO> data = response.body().data;
                    if (data == null || data.isEmpty()) {
                        Toast.makeText(PickingListActivity.this, "当前没有待发货的采摘任务", Toast.LENGTH_SHORT).show();
                    }
                    recyclerView.setAdapter(new PickingListAdapter(data));
                }
            }

            @Override
            public void onFailure(Call<Result<List<FarmerPickingVO>>> call, Throwable t) {
                Toast.makeText(PickingListActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}