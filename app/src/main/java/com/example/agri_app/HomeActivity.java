package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agri_app.adapter.ProductAdapter;
import com.example.agri_app.entity.Notice;
import com.example.agri_app.entity.Product;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ViewFlipper vfNotice; // 🌟 声明滚动公告栏组件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 初始化组件
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        vfNotice = findViewById(R.id.vf_notice); // 🌟 绑定公告栏

        // 1. 刚进页面时，加载所有商品和系统公告
        loadProducts("");
        fetchNotices(); // 🌟 加载公告

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

        // 6. 跳转至【发布农产品】页面
        Button btnGoPublish = findViewById(R.id.btn_go_publish);

        // 统一使用 "UserPrefs" 确保能读取到 MainActivity 存入的数据
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // 获取角色信息，默认为 0 (居民)
        int role = sp.getInt("role", 0);

        // 根据角色判断显隐：role == 1 代表农户
        if (role == 1) {
            btnGoPublish.setVisibility(View.VISIBLE); // 农户可见
        } else {
            btnGoPublish.setVisibility(View.GONE);    // 其他角色隐藏
        }

        btnGoPublish.setOnClickListener(v -> {
            startActivity(new Intent(this, PublishActivity.class));
        });
    }

    /**
     * 加载商品列表
     */
    private void loadProducts(String keyword) {
        RetrofitClient.getApi().getProductList(keyword).enqueue(new Callback<Result<List<Product>>>() {
            @Override
            public void onResponse(Call<Result<List<Product>>> call, Response<Result<List<Product>>> response) {
                // 将 getCode() 改为 .code，getData() 改为 .data，getMsg() 改为 .msg
                if (response.body() != null && response.body().code == 200) {
                    recyclerView.setAdapter(new ProductAdapter(response.body().data));
                } else {
                    Toast.makeText(HomeActivity.this, "加载商品失败: " + (response.body() != null ? response.body().msg : "接口异常"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<List<Product>>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 从后端获取公告数据并填充到 ViewFlipper
     */
    private void fetchNotices() {
        RetrofitClient.getApi().getNoticeList().enqueue(new Callback<Result<List<Notice>>>() {
            @Override
            public void onResponse(Call<Result<List<Notice>>> call, Response<Result<List<Notice>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<Notice>> result = response.body();
                    // 将 getCode() 改为 .code，getData() 改为 .data
                    if (result.code == 200 && result.data != null && !result.data.isEmpty()) {
                        setupNoticeFlipper(result.data);
                    } else {
                        // 如果后端没数据，显示一条默认公告
                        showDefaultNotice();
                    }
                } else {
                    showDefaultNotice();
                }
            }

            @Override
            public void onFailure(Call<Result<List<Notice>>> call, Throwable t) {
                // 静默处理网络错误，仅显示默认公告，不打扰用户浏览商品
                showDefaultNotice();
            }
        });
    }

    /**
     * 🌟 新增：动态生成 TextView 并添加到 ViewFlipper 中进行滚动
     */
    private void setupNoticeFlipper(List<Notice> notices) {
        // 先清空之前的视图，防止重复添加
        vfNotice.removeAllViews();

        for (Notice notice : notices) {
            // 动态创建一个 TextView
            TextView textView = new TextView(this);
            // 格式：标题 - 内容
            String displayText = notice.getTitle() + "：" + notice.getContent();
            textView.setText(displayText);
            textView.setTextSize(13f); // 字号稍微调小一点，显得更精致
            textView.setTextColor(getResources().getColor(android.R.color.black));

            // 设置单行显示，超出部分用省略号(...)
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);

            // 给单条公告添加点击事件
            textView.setOnClickListener(v -> {
                // 点击时弹出完整公告内容的 Toast，或者你可以以后扩展成 Dialog
                Toast.makeText(HomeActivity.this, notice.getContent(), Toast.LENGTH_LONG).show();
            });

            // 添加到 ViewFlipper
            vfNotice.addView(textView);
        }

        // 如果有多条公告，才开启自动滚动
        if (notices.size() > 1) {
            vfNotice.startFlipping();
        } else {
            vfNotice.stopFlipping();
        }
    }

    /**
     * 🌟 新增：当没有公告或网络错误时的默认显示
     */
    private void showDefaultNotice() {
        vfNotice.removeAllViews();
        TextView textView = new TextView(this);
        textView.setText("欢迎来到乡村农产品直供社区平台！助农兴农，从你我做起。");
        textView.setTextSize(13f);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);

        vfNotice.addView(textView);
        vfNotice.stopFlipping(); // 只有一条，不需要滚动
    }
}