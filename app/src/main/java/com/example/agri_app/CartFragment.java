package com.example.agri_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agri_app.adapter.CartAdapter;
import com.example.agri_app.entity.CartVO;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 🌟 1. 搬迁布局：使用原本 activity_cart 的内容
        View view = inflater.inflate(R.layout.activity_cart, container, false);

        recyclerView = view.findViewById(R.id.rv_cart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 🌟 2. 逻辑搬迁：注意 this 换成 getContext()
        long userId = getContext().getSharedPreferences("UserPrefs", 0).getLong("userId", -1);
        loadCartData(userId);

        Button btnCheckout = view.findViewById(R.id.btn_checkout);
        btnCheckout.setOnClickListener(v -> {
            RetrofitClient.getApi().createOrder(userId).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null) {
                        Toast.makeText(getContext(), "下单成功！", Toast.LENGTH_SHORT).show();
                        loadCartData(userId); // 刷新
                    }
                }
                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {}
            });
        });

        return view;
    }

    private void loadCartData(long userId) {
        RetrofitClient.getApi().getCartList(userId).enqueue(new Callback<Result<List<CartVO>>>() {
            @Override
            public void onResponse(Call<Result<List<CartVO>>> call, Response<Result<List<CartVO>>> response) {
                if (response.body() != null) {
                    recyclerView.setAdapter(new CartAdapter(response.body().data));
                }
            }
            @Override
            public void onFailure(Call<Result<List<CartVO>>> call, Throwable t) {}
        });
    }
}