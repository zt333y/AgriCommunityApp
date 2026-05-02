package com.example.agri_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.entity.OrderItem;
import com.example.agri_app.entity.Result;
import com.example.agri_app.entity.Review;
import com.example.agri_app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {

    private List<OrderItem> orderItems; // 这个订单里的所有商品
    private int currentIndex = 0;       // 当前正在评价第几个商品
    private long orderId;

    private TextView tvTitle;
    private RatingBar ratingBar;
    private EditText etContent;
    private Button btnSubmit;
    private Button btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        orderId = getIntent().getLongExtra("ORDER_ID", 0);

        tvTitle = findViewById(R.id.tv_review_title);
        ratingBar = findViewById(R.id.rating_bar);
        etContent = findViewById(R.id.et_review_content);
        btnSubmit = findViewById(R.id.btn_submit_review);
        btnSkip = findViewById(R.id.btn_skip_review);

        btnSubmit.setEnabled(false);
        btnSkip.setEnabled(false);
        tvTitle.setText("正在加载商品信息...");

        loadOrderItems();

        btnSubmit.setOnClickListener(v -> submitCurrentReview());
        btnSkip.setOnClickListener(v -> moveToNext());
    }

    private void loadOrderItems() {
        RetrofitClient.getApi().getOrderItems(orderId).enqueue(new Callback<Result<List<OrderItem>>>() {
            @Override
            public void onResponse(Call<Result<List<OrderItem>>> call, Response<Result<List<OrderItem>>> response) {
                if (response.body() != null && response.body().code == 200) {
                    orderItems = response.body().data;
                    if (orderItems != null && !orderItems.isEmpty()) {
                        btnSubmit.setEnabled(true);
                        btnSkip.setEnabled(true);
                        showReviewForCurrentItem();
                    } else {
                        Toast.makeText(ReviewActivity.this, "未找到商品明细", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
            @Override
            public void onFailure(Call<Result<List<OrderItem>>> call, Throwable t) {
                Toast.makeText(ReviewActivity.this, "网络加载失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showReviewForCurrentItem() {
        OrderItem currentItem = orderItems.get(currentIndex);

        tvTitle.setText(String.format("给【%s】打个分吧：(%d/%d)",
                currentItem.productName, currentIndex + 1, orderItems.size()));

        ratingBar.setRating(5);
        etContent.setText("");

        boolean isLast = (currentIndex == orderItems.size() - 1);
        btnSubmit.setText(isLast ? "提 交 评 价" : "评价下一个");
        btnSkip.setText(isLast ? "跳过并完成" : "跳过该商品");
    }

    private void submitCurrentReview() {
        if (orderItems == null || currentIndex >= orderItems.size()) return;

        OrderItem currentItem = orderItems.get(currentIndex);

        Review review = new Review();
        review.setOrderId(orderId);
        review.setProductId(currentItem.productId);

        // 🌟 就是这句立了功，它完全正确地抓取了你滑动的星星数
        review.setScore((int) ratingBar.getRating());

        review.setContent(etContent.getText().toString());
        review.setUserId(getSharedPreferences("UserPrefs", MODE_PRIVATE).getLong("userId", 0));

        btnSubmit.setEnabled(false);
        btnSkip.setEnabled(false);
        btnSubmit.setText("正在提交...");

        RetrofitClient.getApi().addReview(review).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    moveToNext();
                } else {
                    Toast.makeText(ReviewActivity.this, "评价失败，请稍后再试", Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                    btnSkip.setEnabled(true);
                }
            }
            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Toast.makeText(ReviewActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(true);
                btnSkip.setEnabled(true);
            }
        });
    }

    private void moveToNext() {
        currentIndex++;

        if (currentIndex < orderItems.size()) {
            btnSubmit.setEnabled(true);
            btnSkip.setEnabled(true);
            showReviewForCurrentItem();
        } else {
            Toast.makeText(ReviewActivity.this, "已评价", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}