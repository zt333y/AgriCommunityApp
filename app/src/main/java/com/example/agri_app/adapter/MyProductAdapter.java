package com.example.agri_app.adapter;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agri_app.PublishActivity;
import com.example.agri_app.R;
import com.example.agri_app.entity.Product;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProductAdapter extends RecyclerView.Adapter<MyProductAdapter.ViewHolder> {
    private List<Product> productList;

    public MyProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("￥" + product.getPrice() + " / " + product.getUnit());
        holder.tvStock.setText("当前库存: " + product.getStock());

        String displayUrl = product.getImageUrl();
        if (displayUrl != null && displayUrl.contains("/uploads/")) {
            displayUrl = "http://192.168.31.60:8080" + displayUrl.substring(displayUrl.indexOf("/uploads/"));
        }

        Glide.with(holder.itemView.getContext())
                .load(displayUrl)
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.ivProduct);

        // 🌟 状态显示（加上 ▾ 箭头暗示可以点击）
        if (product.getStatus() == null || product.getStatus() == 0) {
            holder.tvStatusBadge.setText("审核中");
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#FF9800"));
        } else if (product.getStatus() == 1) {
            holder.tvStatusBadge.setText("已上架 ▾");
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else if (product.getStatus() == 2) {
            holder.tvStatusBadge.setText("未通过");
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#F44336"));
        } else if (product.getStatus() == 3) {
            holder.tvStatusBadge.setText("已下架 ▾");
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#9E9E9E"));
        }

        // 🌟 核心：点击状态徽章，弹出上下架选择框
        holder.tvStatusBadge.setOnClickListener(v -> {
            // 只有上架(1)或下架(3)的状态允许手动切换，审核中的不允许
            if (product.getStatus() == 1 || product.getStatus() == 3) {
                String[] options = {"重新上架", "下架商品"};
                new AlertDialog.Builder(v.getContext())
                        .setTitle("修改商品状态")
                        .setItems(options, (dialog, which) -> {
                            int newStatus = (which == 0) ? 1 : 3;
                            if (newStatus == product.getStatus()) return; // 没变化就不请求

                            RetrofitClient.getApi().updateProductStatus(product.getId(), newStatus).enqueue(new Callback<Result<String>>() {
                                @Override
                                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                                    if (response.body() != null && response.body().code == 200) {
                                        product.setStatus(newStatus);
                                        notifyItemChanged(position); // 局部刷新UI
                                        Toast.makeText(v.getContext(), response.body().data, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<Result<String>> call, Throwable t) {}
                            });
                        }).show();
            } else {
                Toast.makeText(v.getContext(), "当前状态系统锁定，无法手动修改", Toast.LENGTH_SHORT).show();
            }
        });

        // 修改商品信息
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;
            Product currentProduct = productList.get(currentPosition);

            Intent intent = new Intent(v.getContext(), PublishActivity.class);
            intent.putExtra("EDIT_ID", currentProduct.getId());
            intent.putExtra("EDIT_NAME", currentProduct.getName());
            intent.putExtra("EDIT_PRICE", String.valueOf(currentProduct.getPrice()));
            intent.putExtra("EDIT_STOCK", String.valueOf(currentProduct.getStock()));
            intent.putExtra("EDIT_DESC", currentProduct.getDescription());
            intent.putExtra("EDIT_CATEGORY", currentProduct.getCategory());
            intent.putExtra("EDIT_UNIT", currentProduct.getUnit());
            intent.putExtra("EDIT_IMAGE", currentProduct.getImageUrl());
            v.getContext().startActivity(intent);
        });

        // 彻底删除
        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(v.getContext())
                    .setTitle("操作确认")
                    .setMessage("确定要彻底删除 [" + productList.get(currentPosition).getName() + "] 吗？删除后不可恢复。")
                    .setPositiveButton("确定", (dialog, which) -> {
                        RetrofitClient.getApi().deleteProduct(productList.get(currentPosition).getId()).enqueue(new Callback<Result<String>>() {
                            @Override
                            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                                if (response.body() != null && response.body().code == 200) {
                                    Toast.makeText(v.getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                    productList.remove(currentPosition);
                                    notifyItemRemoved(currentPosition);
                                    notifyItemRangeChanged(currentPosition, productList.size());
                                }
                            }
                            @Override
                            public void onFailure(Call<Result<String>> call, Throwable t) {}
                        });
                    })
                    .setNegativeButton("取消", null).show();
        });
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct, btnDelete;
        TextView tvName, tvStock, tvPrice, tvStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}