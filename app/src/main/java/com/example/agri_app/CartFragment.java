package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
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
        View view = inflater.inflate(R.layout.activity_cart, container, false);

        recyclerView = view.findViewById(R.id.rv_cart);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        SharedPreferences sp = getContext().getSharedPreferences("UserPrefs", 0);
        long userId = sp.getLong("userId", -1);
        loadCartData(userId);

        Button btnCheckout = view.findViewById(R.id.btn_checkout);
        btnCheckout.setOnClickListener(v -> {
            // 🌟 1. 读取手机里保存的最新收货地址
            String currentAddress = getContext().getSharedPreferences("UserPrefs", 0).getString("address", "");

            // 🌟 2. 如果没地址，强制拦截并跳转到地址填写页！
            if (currentAddress == null || currentAddress.trim().isEmpty()) {
                Toast.makeText(getContext(), "⚠️ 必须填写收货地址才能结算！", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getContext(), AddressActivity.class));
                return; // 终止执行
            }

            // 🌟 3. 把地址一起发给后端生成订单！
            RetrofitClient.getApi().createOrder(userId, currentAddress).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(getContext(), "下单成功！商家将配送至: " + currentAddress, Toast.LENGTH_LONG).show();
                        loadCartData(userId);
                    } else {
                        Toast.makeText(getContext(), "结算失败", Toast.LENGTH_SHORT).show();
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
                    if (recyclerView != null) {
                        recyclerView.setAdapter(new CartAdapter(response.body().data));
                    }
                }
            }
            @Override
            public void onFailure(Call<Result<List<CartVO>>> call, Throwable t) {}
        });
    }
}