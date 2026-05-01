package com.example.agri_app.adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agri_app.ProductDetailActivity;
import com.example.agri_app.R;
import com.example.agri_app.entity.Cart;
import com.example.agri_app.entity.Product;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> productList;

    public ProductAdapter(List<Product> list) {
        this.productList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = productList.get(position);

        holder.tvName.setText(p.getName());
        holder.tvDesc.setText(p.getDescription());

        // 显示真实价格单位
        holder.tvPrice.setText("￥" + p.getPrice() + " / " + p.getUnit());

        // 展示后端的销量和评分
        int sales = p.getSales() != null ? p.getSales() : 0;
        double rating = p.getRating() != null ? p.getRating() : 5.0;
        holder.tvSalesRating.setText(String.format("已售 %d | 评分 %.1f", sales, rating));

        // 使用 Glide 加载图片
        String imgUrl = p.getImageUrl();
        if (imgUrl != null && imgUrl.contains("localhost")) {
            imgUrl = imgUrl.replace("localhost", "192.168.31.61");
        }

        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imgUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.mipmap.ic_launcher);
        }

        // 🌟 修复：加入购物车的写法
        holder.ivAddCart.setOnClickListener(v -> {
            SharedPreferences sp = v.getContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
            Long userId = sp.getLong("userId", 0L);
            if (userId == 0) {
                Toast.makeText(v.getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🌟 核心修正：使用带参数的构造函数创建购物车对象
            Cart cart = new Cart(userId, p.getId(), 1);

            // 🌟 核心修正：调用 addCart 接口
            RetrofitClient.getApi().addCart(cart).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(v.getContext(), "🛒 成功加入购物车！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "加入失败：" + (response.body() != null ? response.body().msg : ""), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(v.getContext(), "网络异常", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 点击商品跳转到详情页
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("ID", p.getId());
            intent.putExtra("NAME", p.getName());
            intent.putExtra("PRICE", p.getPrice());
            intent.putExtra("DESC", p.getDescription());
            intent.putExtra("IMAGE_URL", p.getImageUrl());
            // 传递产地和单位
            intent.putExtra("UNIT", p.getUnit());
            intent.putExtra("FARMER_ADDRESS", p.getFarmerAddress());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivAddCart;
        TextView tvName, tvDesc, tvPrice, tvSalesRating;

        public ViewHolder(View view) {
            super(view);
            ivImage = view.findViewById(R.id.iv_product_image);
            tvName = view.findViewById(R.id.tv_product_name);
            tvDesc = view.findViewById(R.id.tv_product_desc);
            tvPrice = view.findViewById(R.id.tv_product_price);

            tvSalesRating = view.findViewById(R.id.tv_sales_rating);
            ivAddCart = view.findViewById(R.id.iv_add_cart);
        }
    }
}