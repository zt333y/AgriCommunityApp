package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<CartVO> currentCartList;

    // 绑定合计金额的 TextView
    private TextView tvTotalPrice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_cart, container, false);

        recyclerView = view.findViewById(R.id.rv_cart);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // 绑定控件
        tvTotalPrice = view.findViewById(R.id.tv_total_price);

        SharedPreferences sp = getContext().getSharedPreferences("UserPrefs", 0);
        long userId = sp.getLong("userId", -1);

        loadCartData(userId); // 加载购物车数据

        Button btnCheckout = view.findViewById(R.id.btn_checkout);
        btnCheckout.setOnClickListener(v -> {
            String currentAddress = getContext().getSharedPreferences("UserPrefs", 0).getString("address", "");

            if (currentAddress == null || currentAddress.trim().isEmpty()) {
                Toast.makeText(getContext(), "必须填写收货地址才能结算", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getContext(), AddressActivity.class));
                return;
            }

            if (currentCartList == null || currentCartList.isEmpty()) {
                Toast.makeText(getContext(), "购物车是空的，先去挑点东西吧", Toast.LENGTH_SHORT).show();
                return;
            }

            // 计算当前总价传给弹窗
            double totalAmount = calculateTotal(currentCartList);
            showPaymentDialog(userId, currentAddress, totalAmount);
        });

        return view;
    }

    // 专门算钱的方法
    private double calculateTotal(List<CartVO> list) {
        double total = 0.0;
        if (list != null) {
            for (CartVO item : list) {
                if (item.getPrice() != null && item.getQuantity() != null) {
                    total += (item.getPrice() * item.getQuantity());
                }
            }
        }
        return total;
    }

    private void loadCartData(long userId) {
        RetrofitClient.getApi().getCartList(userId).enqueue(new Callback<Result<List<CartVO>>>() {
            @Override
            public void onResponse(Call<Result<List<CartVO>>> call, Response<Result<List<CartVO>>> response) {
                if (response.body() != null) {
                    currentCartList = response.body().data;

                    if (recyclerView != null) {
                        // 🌟 核心修改：这里传入了 CartAdapter 要求的 OnCartChangeListener 监听器！
                        // 只要在适配器里点击了 加号、减号、垃圾桶，就会触发下面括号里的重新算钱逻辑！
                        recyclerView.setAdapter(new CartAdapter(currentCartList, () -> {
                            double newTotal = calculateTotal(currentCartList);
                            if (tvTotalPrice != null) {
                                tvTotalPrice.setText(String.format("合计: ￥%.2f", newTotal));
                            }
                        }));
                    }

                    // 拿到数据后，立刻算钱并更新左下角的“合计: ￥XXX”文字（初次渲染）
                    double totalAmount = calculateTotal(currentCartList);
                    if (tvTotalPrice != null) {
                        tvTotalPrice.setText(String.format("合计: ￥%.2f", totalAmount));
                    }
                }
            }
            @Override
            public void onFailure(Call<Result<List<CartVO>>> call, Throwable t) {}
        });
    }

    private void showPaymentDialog(long userId, String address, double totalAmount) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_payment, null);

        TextView tvAmount = dialogView.findViewById(R.id.tv_pay_amount);
        tvAmount.setText(String.format("￥%.2f", totalAmount));

        // 🌟🌟🌟 新增：在这里获取 XML 里的 TextView，并把真实地址 set 进去
        TextView tvPayAddress = dialogView.findViewById(R.id.tv_pay_address);
        if (tvPayAddress != null) {
            tvPayAddress.setText(address);
        }

        Button btnPay = dialogView.findViewById(R.id.btn_confirm_pay);
        btnPay.setOnClickListener(v -> {
            btnPay.setText("支付处理中...");
            btnPay.setEnabled(false);

            RetrofitClient.getApi().createOrder(userId, address).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    dialog.dismiss();
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(getContext(), "支付成功！商家将配送至: " + address, Toast.LENGTH_LONG).show();
                        loadCartData(userId); // 支付成功后重新加载（清空）购物车
                    } else {
                        Toast.makeText(getContext(), "结算失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "网络异常", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }
}