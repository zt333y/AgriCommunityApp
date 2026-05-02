package com.example.agri_app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView tvMarquee;

    // 🌟 新增：分类导航栏容器和数据
    private LinearLayout layoutCategories;
    private final String[] CATEGORIES = {"全部分类", "新鲜水果", "有机蔬菜", "肉禽蛋品", "粮油调味", "农副加工", "其他"};
    private TextView currentSelectedTab = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 绑定跑马灯控件
        tvMarquee = view.findViewById(R.id.tv_marquee_notice);
        tvMarquee.setSelected(true);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 🌟 绑定并初始化分类导航栏
        layoutCategories = view.findViewById(R.id.layout_categories);
        setupCategoryTabs();

        // 搜索功能逻辑
        EditText etSearch = view.findViewById(R.id.et_search);
        view.findViewById(R.id.btn_search).setOnClickListener(v -> loadProducts(etSearch.getText().toString().trim()));

        // 初始化加载数据
        loadNotices();
        loadProducts(""); // 初始加载全部商品

        return view;
    }

    // ==========================================
    // 🌟 核心新增：动态生成横向滑动的分类按钮
    // ==========================================
    private void setupCategoryTabs() {
        for (int i = 0; i < CATEGORIES.length; i++) {
            String category = CATEGORIES[i];
            TextView tv = new TextView(getContext());
            tv.setText(category);
            tv.setTextSize(14);

            // 设置内边距，让它看起来像个胶囊按钮
            tv.setPadding(40, 15, 40, 15);
            tv.setBackgroundResource(R.drawable.shape_search_bar);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 0, 10, 0); // 设置按钮之间的左右间距
            tv.setLayoutParams(params);

            // 默认选中第一个 "全部分类"
            if (i == 0) {
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // 农场绿
                currentSelectedTab = tv;
            } else {
                tv.setTextColor(Color.parseColor("#333333"));
                tv.setBackgroundTintList(null);
            }

            // 点击分类按钮的事件
            tv.setOnClickListener(v -> {
                // 1. 恢复上一个选中按钮的默认颜色
                if (currentSelectedTab != null) {
                    currentSelectedTab.setTextColor(Color.parseColor("#333333"));
                    currentSelectedTab.setBackgroundTintList(null);
                }

                // 2. 让当前点击的按钮变绿
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                currentSelectedTab = tv;

                // 3. 触发后端查询（如果是全部分类，就传空字符串；否则传分类名）
                String searchKeyword = category.equals("全部分类") ? "" : category;
                loadProducts(searchKeyword);
            });

            // 将生成的按钮加到横向滑动容器里
            layoutCategories.addView(tv);
        }
    }

    private void loadNotices() {
        RetrofitClient.getApi().getNoticeList().enqueue(new Callback<Result<List<Notice>>>() {
            @Override
            public void onResponse(Call<Result<List<Notice>>> call, Response<Result<List<Notice>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    List<Notice> notices = response.body().data;
                    if (notices != null && !notices.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Notice n : notices) {
                            sb.append(" 【").append(n.getTitle()).append("】").append(n.getContent()).append("    ");
                        }
                        tvMarquee.setText(sb.toString());
                    } else {
                        tvMarquee.setText("欢迎来到乡村农产品直供社区！");
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<List<Notice>>> call, Throwable t) {
                tvMarquee.setText("系统公告加载失败，请检查网络连接...");
            }
        });
    }

    private void loadProducts(String keyword) {
        RetrofitClient.getApi().getProductList(keyword).enqueue(new Callback<Result<List<Product>>>() {
            @Override
            public void onResponse(Call<Result<List<Product>>> call, Response<Result<List<Product>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    recyclerView.setAdapter(new ProductAdapter(response.body().data));
                } else {
                    Toast.makeText(getContext(), "获取商品失败，请检查接口", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Result<List<Product>>> call, Throwable t) {
                Toast.makeText(getContext(), "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}