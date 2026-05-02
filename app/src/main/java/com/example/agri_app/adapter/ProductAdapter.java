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
        holder.tvPrice.setText("￥" + p.getPrice() + " / " + (p.getUnit() != null ? p.getUnit() : "件"));

        int sales = p.getSales() != null ? p.getSales() : 0;
        Double rating = p.getRating();
        if (rating == null || rating == 0) {
            holder.tvSalesRating.setText(String.format("已售 %d | 评分 无", sales));
        } else {
            holder.tvSalesRating.setText(String.format("已售 %d | 评分 %.1f", sales, rating));
        }
        // 🌟 终极修复：暴力截断旧 IP，强行拼接当前绝对正确的 IP！
        String imgUrl = p.getImageUrl();
        if (imgUrl != null && imgUrl.contains("/uploads/")) {
            imgUrl = "http://192.168.31.60:8080" + imgUrl.substring(imgUrl.indexOf("/uploads/"));
        }

        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(imgUrl).placeholder(R.mipmap.ic_launcher).into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.mipmap.ic_launcher);
        }

        if (holder.ivAddCart != null) {
            holder.ivAddCart.setOnClickListener(v -> {
                SharedPreferences sp = v.getContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
                Long userId = sp.getLong("userId", 0L);
                if (userId == 0) {
                    Toast.makeText(v.getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                    return;
                }

                Cart cart = new Cart(userId, p.getId(), 1);
                RetrofitClient.getApi().addCart(cart).enqueue(new Callback<Result<String>>() {
                    @Override
                    public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                        if (response.body() != null && response.body().code == 200) {
                            Toast.makeText(v.getContext(), "成功加入购物车", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(v.getContext(), "加入失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Result<String>> call, Throwable t) {
                        Toast.makeText(v.getContext(), "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("ID", p.getId());
            intent.putExtra("NAME", p.getName());
            intent.putExtra("PRICE", p.getPrice());
            intent.putExtra("DESC", p.getDescription());

            // 同样截断旧IP传给详情页
            String finalImgUrl = p.getImageUrl();
            if (finalImgUrl != null && finalImgUrl.contains("/uploads/")) {
                finalImgUrl = "http://192.168.31.60:8080" + finalImgUrl.substring(finalImgUrl.indexOf("/uploads/"));
            }
            intent.putExtra("IMAGE_URL", finalImgUrl);
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