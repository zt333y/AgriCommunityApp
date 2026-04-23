package com.example.agri_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 绑定跑马灯控件，并强制获取焦点（Android里只有拿到焦点的TextView才会滚动）
        tvMarquee = view.findViewById(R.id.tv_marquee_notice);
        tvMarquee.setSelected(true);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 搜索功能逻辑
        EditText etSearch = view.findViewById(R.id.et_search);
        view.findViewById(R.id.btn_search).setOnClickListener(v -> loadProducts(etSearch.getText().toString().trim()));

        // 初始化加载数据
        loadNotices(); // 🌟 加载公告
        loadProducts(""); // 加载商品列表

        return view;
    }

    // 🌟 核心新增：加载公告并拼接为滚动字符串
    private void loadNotices() {
        RetrofitClient.getApi().getNoticeList().enqueue(new Callback<Result<List<Notice>>>() {
            @Override
            public void onResponse(Call<Result<List<Notice>>> call, Response<Result<List<Notice>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    List<Notice> notices = response.body().data;
                    if (notices != null && !notices.isEmpty()) {
                        // 把后端传来的多条公告拼成一长串，中间用空格隔开
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
                    // 🌟 如果后端返回失败，弹出提示
                    Toast.makeText(getContext(), "获取商品失败，请检查接口！", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Result<List<Product>>> call, Throwable t) {
                // 🌟 如果网络不通，把真实错误原因弹出来
                Toast.makeText(getContext(), "网络异常：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}