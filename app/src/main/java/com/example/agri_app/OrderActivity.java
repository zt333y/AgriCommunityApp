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
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView rvOrder;
    private TabLayout tabLayout;
    private View layoutEmpty;
    private Long currentUserId;

    // 核心：用来保存从服务器拿回来的【所有】订单
    private List<OrderVO> allOrders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        // 1. 绑定控件
        rvOrder = findViewById(R.id.rv_order);
        tabLayout = findViewById(R.id.tab_layout_order);
        layoutEmpty = findViewById(R.id.layout_empty_order);

        rvOrder.setLayoutManager(new LinearLayoutManager(this));

        // 2. 初始化分类 Tab 标签
        setupTabs();

        // 3. 获取当前登录用户的 ID
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = sp.getLong("userId", 1L);

        // 4. 去后端拉取全量订单数据
        loadOrderData();
    }

    // 🌟 初始化顶部分类栏
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("全部"));     // 索引 0
        tabLayout.addTab(tabLayout.newTab().setText("待发货"));   // 索引 1 (状态 0)
        tabLayout.addTab(tabLayout.newTab().setText("已发货"));   // 索引 2 (状态 1)
        tabLayout.addTab(tabLayout.newTab().setText("待提货"));   // 索引 3 (状态 4)
        tabLayout.addTab(tabLayout.newTab().setText("待评价"));   // 索引 4 (状态 2)
        tabLayout.addTab(tabLayout.newTab().setText("已完成"));   // 索引 5 (状态 3)
        // 🌟 新增：售后分类 Tab
        tabLayout.addTab(tabLayout.newTab().setText("售后/退换")); // 索引 6 (状态 >= 5)

        // 监听 Tab 的点击事件
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 当用户点击不同的 Tab 时，重新过滤数据并渲染
                filterOrdersByStatus(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // 🌟 拉取网络数据（只需拉取一次）
    private void loadOrderData() {
        RetrofitClient.getApi().getOrderList(currentUserId).enqueue(new Callback<Result<List<OrderVO>>>() {
            @Override
            public void onResponse(Call<Result<List<OrderVO>>> call, Response<Result<List<OrderVO>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    // 把拿到的数据存到“总库”里面
                    allOrders = response.body().data != null ? response.body().data : new ArrayList<>();

                    // 默认显示第一个 Tab (全部订单)
                    filterOrdersByStatus(tabLayout.getSelectedTabPosition());
                } else {
                    Toast.makeText(OrderActivity.this, "未获取到订单", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<List<OrderVO>>> call, Throwable t) {
                Toast.makeText(OrderActivity.this, "加载订单失败: 网络异常", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🌟 核心过滤逻辑：根据选中的 Tab 筛选要展示的数据
    private void filterOrdersByStatus(int tabPosition) {
        List<OrderVO> displayList = new ArrayList<>();

        if (tabPosition == 0) {
            // 全部订单，不用过滤
            displayList.addAll(allOrders);
        } else if (tabPosition == 6) {
            // 🌟 售后专区逻辑：把所有状态码大于等于 5 (即处于各种售后阶段) 的订单过滤出来
            for (OrderVO order : allOrders) {
                if (order.getStatus() != null && order.getStatus() >= 5) {
                    displayList.add(order);
                }
            }
        } else {
            // 找出每个 Tab 对应的真实业务状态码
            int targetStatus = -1;
            switch (tabPosition) {
                case 1: targetStatus = 0; break; // 待发货 (Status 0)
                case 2: targetStatus = 1; break; // 已发货/运输中 (Status 1)
                case 3: targetStatus = 4; break; // 待提货/团长已收货 (Status 4)
                case 4: targetStatus = 2; break; // 待评价 (Status 2)
                case 5: targetStatus = 3; break; // 已完成 (Status 3)
            }

            // 在总库里遍历，把符合状态的订单挑出来
            for (OrderVO order : allOrders) {
                if (order.getStatus() != null && order.getStatus() == targetStatus) {
                    displayList.add(order);
                }
            }
        }

        // 渲染界面
        if (displayList.isEmpty()) {
            rvOrder.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvOrder.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            rvOrder.setAdapter(new OrderAdapter(displayList));
        }
    }
}