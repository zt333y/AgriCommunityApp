package com.example.agri_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.entity.Result;
import com.example.agri_app.entity.Review; // 需要你在 entity 包下创建 Review 类
import com.example.agri_app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // 获取上一个页面传来的订单ID和商品ID
        long orderId = getIntent().getLongExtra("ORDER_ID", 0);
        long productId = getIntent().getLongExtra("PRODUCT_ID", 1L);

        // 绑定控件
        RatingBar ratingBar = findViewById(R.id.rating_bar);
        EditText etContent = findViewById(R.id.et_review_content);
        Button btnSubmit = findViewById(R.id.btn_submit_review);

        btnSubmit.setOnClickListener(v -> {
            Review review = new Review();
            review.setOrderId(orderId);
            review.setProductId(productId); // 把商品ID也传给后端
            review.setScore((int) ratingBar.getRating());
            review.setContent(etContent.getText().toString());
            // 从缓存获取当前用户ID
            review.setUserId(getSharedPreferences("UserPrefs", MODE_PRIVATE).getLong("userId", 0));

            // 发起网络请求
            RetrofitClient.getApi().addReview(review).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(ReviewActivity.this, "🎉 评价成功，感谢您的支持！", Toast.LENGTH_SHORT).show();
                        finish(); // 评价完关闭当前页面，自动回到订单列表
                    } else {
                        Toast.makeText(ReviewActivity.this, "评价失败，请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(ReviewActivity.this, "网络异常，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}