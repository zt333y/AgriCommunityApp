package com.example.agri_app.adapter;

import android.app.AlertDialog;
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
        // 绑定单件商品卡片布局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // 1. 设置商品基础信息
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("￥" + product.getPrice());
        holder.tvStock.setText("当前库存: " + product.getStock() + " 份");

        // 2. 加载商品图片
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.ivProduct);

        // 3. 根据审核状态动态改变标签颜色
        if (product.getStatus() == null || product.getStatus() == 0) {
            holder.tvStatusBadge.setText("⏳ 审核中");
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#FF9800")); // 橙色
        } else if (product.getStatus() == 1) {
            holder.tvStatusBadge.setText("✅ 已上架");
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // 绿色
        } else if (product.getStatus() == 2) {
            holder.tvStatusBadge.setText("❌ 未通过");
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#F44336")); // 红色
        }

// 4. 🌟 处理删除按钮的点击事件
        holder.itemView.findViewById(R.id.btn_delete).setOnClickListener(v -> {

            // 🌟 核心修复点 1：在点击瞬间，获取这个卡片此时此刻的真实位置！
            int currentPosition = holder.getAdapterPosition();

            // 🌟 核心修复点 2：防止用户疯狂连击导致下标越界崩溃
            if (currentPosition == RecyclerView.NO_POSITION) return;

            // 重新获取正确的商品对象
            Product currentProduct = productList.get(currentPosition);

            // 弹出确认对话框
            new AlertDialog.Builder(v.getContext())
                    .setTitle("操作确认")
                    .setMessage("确定要下架/删除商品 [" + currentProduct.getName() + "] 吗？删除后不可恢复。")
                    .setPositiveButton("确定", (dialog, which) -> {
                        // 发起网络请求删除商品
                        RetrofitClient.getApi().deleteProduct(currentProduct.getId()).enqueue(new Callback<Result<String>>() {
                            @Override
                            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                                if (response.body() != null && response.body().code == 200) {
                                    Toast.makeText(v.getContext(), "删除成功", Toast.LENGTH_SHORT).show();

                                    // 🌟 核心修复点 3：使用最新的 currentPosition 去删除和刷新
                                    productList.remove(currentPosition);
                                    notifyItemRemoved(currentPosition);
                                    notifyItemRangeChanged(currentPosition, productList.size());
                                } else {
                                    Toast.makeText(v.getContext(), "删除失败", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Result<String>> call, Throwable t) {
                                Toast.makeText(v.getContext(), "网络异常", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    // 绑定 XML 中的控件
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvStock, tvPrice, tvStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
        }
    }
}