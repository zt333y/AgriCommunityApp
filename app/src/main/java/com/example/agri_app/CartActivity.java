package com.example.agri_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

    private RecyclerView rvCart;  // 统一名字为 rvCart
    private TextView tvTotalPrice;
    private Long currentUserId;   // 补充缺失的变量！

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rv_cart);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        Button btnCheckout = findViewById(R.id.btn_checkout);

        rvCart.setLayoutManager(new LinearLayoutManager(this));

        // 🌟 关键补充：从本地缓存获取真实的用户ID
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = sp.getLong("userId", 1L); // 如果没拿到，暂时默认给1号用户

        // 去后端拉取购物车数据
        loadCartData();

        btnCheckout.setOnClickListener(v -> {
            // 发起请求，传入刚才获取到的真实用户ID
            RetrofitClient.getApi().createOrder(currentUserId).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(CartActivity.this, "🎉 支付成功！订单已生成！", Toast.LENGTH_LONG).show();
                        // 订单生成后，购物车应该空了，重新刷新一下列表就会显示“空空如也”
                        loadCartData();
                    } else {
                        Toast.makeText(CartActivity.this, "结算失败: " + (response.body()!=null ? response.body().msg : ""), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(CartActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // 每次进入页面，去后端拉取购物车列表
    private void loadCartData() {
        RetrofitClient.getApi().getCartList(currentUserId).enqueue(new Callback<Result<List<CartVO>>>() {
            @Override
            public void onResponse(Call<Result<List<CartVO>>> call, Response<Result<List<CartVO>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    List<CartVO> list = response.body().data;

                    View layoutEmpty = findViewById(R.id.layout_empty_cart);

                    // 🌟 核心判断逻辑：如果没数据
                    if (list == null || list.isEmpty()) {
                        rvCart.setVisibility(View.GONE);     // 隐藏列表
                        layoutEmpty.setVisibility(View.VISIBLE); // 显示空提示
                        tvTotalPrice.setText("合计: ￥0.00");
                    } else {
                        // 如果有数据
                        rvCart.setVisibility(View.VISIBLE);  // 显示列表
                        layoutEmpty.setVisibility(View.GONE);    // 隐藏空提示

                        rvCart.setAdapter(new CartAdapter(list));

                        double total = 0;
                        for (CartVO item : list) {
                            total += (item.getPrice() * item.getQuantity());
                        }
                        tvTotalPrice.setText(String.format("合计: ￥%.2f", total));
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<List<CartVO>>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "获取购物车失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}