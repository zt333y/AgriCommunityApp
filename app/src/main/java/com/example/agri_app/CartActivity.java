package com.example.agri_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView tvTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_recycler_view);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        android.widget.TextView btnCheckout = findViewById(R.id.btn_checkout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 去后端拉取购物车数据
        loadCartData();

        btnCheckout.setOnClickListener(v -> {
            // 发起请求，传入用户ID (假设当前登录的是1)
            RetrofitClient.getApi().createOrder(1L).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(CartActivity.this, "🎉 支付成功！订单已生成！", Toast.LENGTH_LONG).show();
                        // 订单生成后，购物车应该空了，我们重新刷新一下列表
                        loadCartData();
                    } else {
                        Toast.makeText(CartActivity.this, "结算失败: " + (response.body()!=null?response.body().msg:""), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(CartActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadCartData() {
        // 传入userId=1
        RetrofitClient.getApi().getCartList(1L).enqueue(new Callback<Result<List<CartVO>>>() {
            @Override
            public void onResponse(Call<Result<List<CartVO>>> call, Response<Result<List<CartVO>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    List<CartVO> list = response.body().data;
                    recyclerView.setAdapter(new CartAdapter(list));

                    // 计算总价
                    double total = 0;
                    for (CartVO item : list) {
                        total += (item.price * item.quantity);
                    }
                    tvTotalPrice.setText("￥" + String.format("%.2f", total));
                }
            }

            @Override
            public void onFailure(Call<Result<List<CartVO>>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "获取购物车失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}