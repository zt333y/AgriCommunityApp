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
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        TextView tvName = findViewById(R.id.detail_name);
        TextView tvPrice = findViewById(R.id.detail_price);
        TextView tvDesc = findViewById(R.id.detail_desc);
        ImageView ivImage = findViewById(R.id.detail_image);
        Button btnAddCart = findViewById(R.id.btn_add_cart);
        TextView tvOrigin = findViewById(R.id.tv_origin);

        long productId = getIntent().getLongExtra("ID", 0);
        String name = getIntent().getStringExtra("NAME");
        double price = getIntent().getDoubleExtra("PRICE", 0.0);
        String desc = getIntent().getStringExtra("DESC");
        String imageUrl = getIntent().getStringExtra("IMAGE_URL");
        String unit = getIntent().getStringExtra("UNIT");
        String fullAddress = getIntent().getStringExtra("FARMER_ADDRESS");

        tvName.setText(name);
        tvDesc.setText(desc);
        tvPrice.setText("￥" + price + " / " + (unit != null ? unit : "件"));

        if (tvOrigin != null) {
            String origin = "源产地直发";
            if (fullAddress != null && fullAddress.contains("市")) {
                int provIdx = fullAddress.indexOf("省");
                int cityIdx = fullAddress.indexOf("市");
                if (cityIdx != -1) {
                    origin = fullAddress.substring(provIdx != -1 ? provIdx + 1 : 0, cityIdx + 1);
                }
            }
            tvOrigin.setText("产地: " + origin);
            tvOrigin.setVisibility(View.VISIBLE);
        }

        // 依然保留图片 IP 修复逻辑
        if (imageUrl != null && imageUrl.contains("/uploads/")) {
            imageUrl = "http://192.168.31.60:8080" + imageUrl.substring(imageUrl.indexOf("/uploads/"));
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(R.mipmap.ic_launcher).into(ivImage);
        }

        if (productId != 0) {
            loadProductReviews(productId);
        }

        btnAddCart.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long currentUserId = sp.getLong("userId", -1L);
            if (currentUserId == -1L) { Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show(); return; }
            Cart cart = new Cart(currentUserId, productId, 1);
            RetrofitClient.getApi().addCart(cart).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(ProductDetailActivity.this, "已加入购物车", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "加入失败", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<Result<String>> call, Throwable t) {}
            });
        });
    }

    private void loadProductReviews(long productId) {
        LinearLayout reviewsContainer = findViewById(R.id.layout_reviews_container);
        RatingBar rbTotal = findViewById(R.id.rb_total_rating);
        TextView tvTotalScore = findViewById(R.id.tv_total_score);

        RetrofitClient.getApi().getProductReviews(productId).enqueue(new Callback<Result<List<ReviewVO>>>() {
            @Override
            public void onResponse(Call<Result<List<ReviewVO>>> call, Response<Result<List<ReviewVO>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    List<ReviewVO> reviews = response.body().data;
                    reviewsContainer.removeAllViews();
                    if (reviews == null || reviews.isEmpty()) {
                        TextView tvEmpty = new TextView(ProductDetailActivity.this);
                        tvEmpty.setText("暂无评价，快来抢沙发吧");
                        tvEmpty.setTextColor(Color.GRAY);
                        tvEmpty.setPadding(0, 20, 0, 20);
                        reviewsContainer.addView(tvEmpty);
                        return;
                    }

                    float totalScore = 0;
                    for (ReviewVO review : reviews) {
                        // 🌟 核心：使用 score
                        totalScore += (review.score != null ? review.score : 5);
                    }
                    float averageScore = totalScore / reviews.size();

                    rbTotal.setVisibility(View.VISIBLE);
                    rbTotal.setRating(averageScore);
                    tvTotalScore.setText(String.format("%.1f分 (%d条)", averageScore, reviews.size()));
                    tvTotalScore.setTextColor(Color.parseColor("#FF9800"));
                    tvTotalScore.setTypeface(null, android.graphics.Typeface.BOLD);

                    for (ReviewVO review : reviews) {
                        View reviewView = getLayoutInflater().inflate(R.layout.item_review, reviewsContainer, false);
                        TextView tvUser = reviewView.findViewById(R.id.tv_review_user);
                        TextView tvContent = reviewView.findViewById(R.id.tv_review_content);
                        RatingBar rbRating = reviewView.findViewById(R.id.rb_review_rating);

                        // 🌟 绑定头像 ImageView控件
                        ImageView ivAvatar = reviewView.findViewById(R.id.iv_reviewer_avatar);

                        // 🌟 核心：调用真正的 username 和 score
                        tvUser.setText(review.username != null ? review.username : "匿名用户");
                        tvContent.setText(review.content);
                        rbRating.setRating(review.score != null ? review.score : 5);

                        // 🌟🌟 核心新增：加载头像并拼接真实服务器 IP，设置圆角
                        String avatarUrl = review.getUserAvatar(); // 确保你 Android 端的 ReviewVO 里已经加了这个字段的 Getter 方法
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            String displayUrl = avatarUrl;
                            if (displayUrl.contains("/uploads/")) {
                                displayUrl = "http://192.168.31.60:8080" + displayUrl.substring(displayUrl.indexOf("/uploads/"));
                            }

                            Glide.with(ProductDetailActivity.this)
                                    .load(displayUrl)
                                    .placeholder(R.mipmap.ic_launcher_round) // 默认头像
                                    .circleCrop() // 将头像裁剪为圆形
                                    .into(ivAvatar);
                        } else {
                            // 没有头像时，显示默认图标
                            ivAvatar.setImageResource(R.mipmap.ic_launcher_round);
                        }

                        reviewsContainer.addView(reviewView);
                        View line = new View(ProductDetailActivity.this);
                        line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                        line.setBackgroundColor(Color.parseColor("#EEEEEE"));
                        reviewsContainer.addView(line);
                    }
                }
            }
            @Override public void onFailure(Call<Result<List<ReviewVO>>> call, Throwable t) {}
        });
    }
}