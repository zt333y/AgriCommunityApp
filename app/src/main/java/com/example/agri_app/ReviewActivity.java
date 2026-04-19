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
    private Button btnSkip; // 🌟 新增：跳过按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // 获取传来的订单ID
        orderId = getIntent().getLongExtra("ORDER_ID", 0);

        // 绑定控件
        tvTitle = findViewById(R.id.tv_review_title);
        ratingBar = findViewById(R.id.rating_bar);
        etContent = findViewById(R.id.et_review_content);
        btnSubmit = findViewById(R.id.btn_submit_review);
        btnSkip = findViewById(R.id.btn_skip_review); // 🌟 绑定跳过按钮

        // 锁定按钮，防止数据没拉下来就乱点
        btnSubmit.setEnabled(false);
        btnSkip.setEnabled(false);
        tvTitle.setText("正在加载商品信息...");

        // 1. 先去后端把这个订单里包含的所有商品拉取下来
        loadOrderItems();

        // 2. 绑定按钮事件
        btnSubmit.setOnClickListener(v -> submitCurrentReview());

        // 🌟 3. 绑定跳过事件：不发请求，直接进入下一个
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
                        btnSkip.setEnabled(true); // 🌟 激活跳过按钮
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

    // 将当前要评价的商品信息展示在屏幕上
    private void showReviewForCurrentItem() {
        OrderItem currentItem = orderItems.get(currentIndex);

        tvTitle.setText(String.format("给【%s】打个分吧：(%d/%d)",
                currentItem.productName, currentIndex + 1, orderItems.size()));

        // 清空上一个商品填写的痕迹
        ratingBar.setRating(5);
        etContent.setText("");

        // 如果是最后一个商品了，动态改变按钮文字
        boolean isLast = (currentIndex == orderItems.size() - 1);
        btnSubmit.setText(isLast ? "提 交 评 价" : "提交并评价下一个");
        btnSkip.setText(isLast ? "跳过并完成" : "跳过该商品");
    }

    // 提交当前评价
    private void submitCurrentReview() {
        if (orderItems == null || currentIndex >= orderItems.size()) return;

        OrderItem currentItem = orderItems.get(currentIndex);

        Review review = new Review();
        review.setOrderId(orderId);
        review.setProductId(currentItem.productId);
        review.setScore((int) ratingBar.getRating());
        review.setContent(etContent.getText().toString());
        review.setUserId(getSharedPreferences("UserPrefs", MODE_PRIVATE).getLong("userId", 0));

        // 按钮防连点
        btnSubmit.setEnabled(false);
        btnSkip.setEnabled(false);
        btnSubmit.setText("正在提交...");

        RetrofitClient.getApi().addReview(review).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    // 🌟 提交成功后，调用统一的方法进入下一个
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

    // 🌟 核心提取：进入下一个商品的通用逻辑 (提交成功 或 点击跳过 都会触发这里)
    private void moveToNext() {
        currentIndex++; // 游标加 1

        if (currentIndex < orderItems.size()) {
            // 如果还有商品，恢复按钮并展示下一个
            btnSubmit.setEnabled(true);
            btnSkip.setEnabled(true);
            showReviewForCurrentItem();
        } else {
            // 如果全部评价完了（或全部跳过了），圆满结束！
            Toast.makeText(ReviewActivity.this, "🎉 评价环节已完成，感谢支持！", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}