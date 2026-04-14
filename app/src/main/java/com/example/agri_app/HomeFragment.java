package com.example.agri_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 搜索功能逻辑迁移
        EditText etSearch = view.findViewById(R.id.et_search);
        view.findViewById(R.id.btn_search).setOnClickListener(v -> {
            loadProducts(etSearch.getText().toString().trim());
        });

        loadProducts("");
        return view;
    }

    private void loadProducts(String keyword) {
        RetrofitClient.getApi().getProductList(keyword).enqueue(new Callback<Result<List<Product>>>() {
            @Override
            public void onResponse(Call<Result<List<Product>>> call, Response<Result<List<Product>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    recyclerView.setAdapter(new ProductAdapter(response.body().data));
                }
            }
            @Override
            public void onFailure(Call<Result<List<Product>>> call, Throwable t) {}
        });
    }
}