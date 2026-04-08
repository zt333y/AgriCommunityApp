package com.example.agri_app.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.agri_app.ProductDetailActivity;
import com.example.agri_app.R;
import com.example.agri_app.entity.Product;
import java.util.List;

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
        holder.tvPrice.setText("￥" + p.getPrice());

        // 🌟 核心：使用 Glide 加载图片
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(p.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher) // 加载中显示的默认图
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.mipmap.ic_launcher); // 如果没有图片，给个兜底图
        }

        // 点击商品跳转到详情页
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("ID", p.getId());
            intent.putExtra("NAME", p.getName());
            intent.putExtra("PRICE", p.getPrice());
            intent.putExtra("DESC", p.getDescription());
            intent.putExtra("IMAGE_URL", p.getImageUrl());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvDesc, tvPrice;

        public ViewHolder(View view) {
            super(view);
            ivImage = view.findViewById(R.id.iv_product_image);
            tvName = view.findViewById(R.id.tv_product_name);
            tvDesc = view.findViewById(R.id.tv_product_desc);
            tvPrice = view.findViewById(R.id.tv_product_price);
        }
    }
}