package com.example.agri_app;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.agri_app.entity.Cart;
import com.example.agri_app.entity.Result;
import com.example.agri_app.entity.ReviewVO;
import com.example.agri_app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // 1. 找到界面上的控件
        TextView tvName = findViewById(R.id.detail_name);
        TextView tvPrice = findViewById(R.id.detail_price);
        TextView tvDesc = findViewById(R.id.detail_desc);
        ImageView ivImage = findViewById(R.id.detail_image);
        Button btnAddCart = findViewById(R.id.btn_add_cart);

        // 2. 接收从上一个页面传过来的数据
        long productId = getIntent().getLongExtra("ID", 0);
        String name = getIntent().getStringExtra("NAME");
        double price = getIntent().getDoubleExtra("PRICE", 0.0);
        String desc = getIntent().getStringExtra("DESC");
        String imageUrl = getIntent().getStringExtra("IMAGE_URL");

        // 3. 把数据展示在界面上
        tvName.setText(name);
        tvPrice.setText("￥" + price);
        tvDesc.setText(desc);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(ivImage);
        }

        // 🌟 4. 核心新增：去服务器拉取这件商品的所有的评价！
        if (productId != 0) {
            loadProductReviews(productId);
        }

        // 5. 给加入购物车按钮设置点击事件
        btnAddCart.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long currentUserId = sp.getLong("userId", -1L);

            if (currentUserId == -1L) {
                Toast.makeText(ProductDetailActivity.this, "请先登录！", Toast.LENGTH_SHORT).show();
                return;
            }

            Cart cart = new Cart(currentUserId, productId, 1);

            // 发起网络请求加入购物车
            RetrofitClient.getApi().addCart(cart).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(ProductDetailActivity.this, "太棒了！已加入您的购物车！", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "加入失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ==========================================
    // 🌟 动态加载并渲染评论列表（含计算平均分）
    // ==========================================
    private void loadProductReviews(long productId) {
        LinearLayout reviewsContainer = findViewById(R.id.layout_reviews_container);
        RatingBar rbTotal = findViewById(R.id.rb_total_rating);
        TextView tvTotalScore = findViewById(R.id.tv_total_score);

        RetrofitClient.getApi().getProductReviews(productId).enqueue(new Callback<Result<List<ReviewVO>>>() {
            @Override
            public void onResponse(Call<Result<List<ReviewVO>>> call, Response<Result<List<ReviewVO>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    List<ReviewVO> reviews = response.body().data;
                    reviewsContainer.removeAllViews(); // 先清空之前的视图

                    // 如果没人评价
                    if (reviews == null || reviews.isEmpty()) {
                        TextView tvEmpty = new TextView(ProductDetailActivity.this);
                        tvEmpty.setText("暂无评价，快来抢沙发吧~");
                        tvEmpty.setTextColor(Color.GRAY);
                        tvEmpty.setPadding(0, 20, 0, 20);
                        reviewsContainer.addView(tvEmpty);
                        return;
                    }

                    // =====================================
                    // 🌟 核心算法：累加所有评价的星星，计算平均分
                    // =====================================
                    float totalScore = 0;
                    for (ReviewVO review : reviews) {
                        totalScore += (review.rating != null ? review.rating : 5);
                    }
                    float averageScore = totalScore / reviews.size();

                    // 更新顶部综合评分 UI
                    rbTotal.setVisibility(View.VISIBLE);
                    rbTotal.setRating(averageScore);
                    // 格式化为保留一位小数 (例如 4.5分 (12条))
                    tvTotalScore.setText(String.format("%.1f分 (%d条)", averageScore, reviews.size()));
                    tvTotalScore.setTextColor(Color.parseColor("#FF9800")); // 变成醒目的橙色
                    tvTotalScore.setTypeface(null, android.graphics.Typeface.BOLD); // 加粗

                    // =====================================
                    // 循环遍历评价数据，动态生成视图塞进页面里
                    // =====================================
                    for (ReviewVO review : reviews) {
                        View reviewView = getLayoutInflater().inflate(R.layout.item_review, reviewsContainer, false);

                        TextView tvUser = reviewView.findViewById(R.id.tv_review_user);
                        TextView tvContent = reviewView.findViewById(R.id.tv_review_content);
                        RatingBar rbRating = reviewView.findViewById(R.id.rb_review_rating);

                        tvUser.setText(review.userName != null ? review.userName : "匿名用户");
                        tvContent.setText(review.content);
                        rbRating.setRating(review.rating != null ? review.rating : 5);

                        reviewsContainer.addView(reviewView);

                        // 加一条浅灰色的分割线
                        View line = new View(ProductDetailActivity.this);
                        line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                        line.setBackgroundColor(Color.parseColor("#EEEEEE"));
                        reviewsContainer.addView(line);
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<List<ReviewVO>>> call, Throwable t) {
                tvTotalScore.setText("加载失败");
            }
        });
    }
}