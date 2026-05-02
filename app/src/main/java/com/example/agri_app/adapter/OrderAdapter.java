package com.example.agri_app.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agri_app.R;
import com.example.agri_app.ReviewActivity;
import com.example.agri_app.entity.OrderItem;
import com.example.agri_app.entity.OrderVO;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<OrderVO> orderList;

    public OrderAdapter(List<OrderVO> list) {
        this.orderList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderVO o = orderList.get(position);

        holder.tvOrderNo.setText("订单号: " + o.getOrderNo());
        holder.tvTime.setText("下单时间: " + o.getCreateTime());
        holder.tvAmount.setText("实付: ￥" + o.getTotalAmount());

        // 渲染商品明细
        holder.layoutOrderItems.removeAllViews();
        if (o.getItems() != null && !o.getItems().isEmpty()) {
            holder.layoutOrderItems.setVisibility(View.VISIBLE);
            for (OrderItem item : o.getItems()) {
                LinearLayout itemLayout = new LinearLayout(holder.itemView.getContext());
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(0, 10, 0, 10);
                itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

                ImageView iv = new ImageView(holder.itemView.getContext());
                int imgSize = (int) (50 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
                iv.setLayoutParams(new LinearLayout.LayoutParams(imgSize, imgSize));
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                String imgUrl = item.getImageUrl();
                // 图片 IP 修正
                if (imgUrl != null && imgUrl.contains("/uploads/")) {
                    imgUrl = "http://192.168.31.60:8080" + imgUrl.substring(imgUrl.indexOf("/uploads/"));
                }

                Glide.with(holder.itemView.getContext())
                        .load(imgUrl)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(iv);

                TextView tvName = new TextView(holder.itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                params.setMargins(20, 0, 0, 0);
                tvName.setLayoutParams(params);
                tvName.setText(item.getProductName());
                tvName.setTextColor(Color.parseColor("#333333"));
                tvName.setTextSize(14);

                TextView tvQty = new TextView(holder.itemView.getContext());
                tvQty.setText("x" + item.getQuantity());
                tvQty.setTextColor(Color.parseColor("#666666"));
                tvQty.setTextSize(14);

                itemLayout.addView(iv);
                itemLayout.addView(tvName);
                itemLayout.addView(tvQty);
                holder.layoutOrderItems.addView(itemLayout);
            }
        } else {
            holder.layoutOrderItems.setVisibility(View.GONE);
        }

        // 默认隐藏操作按钮
        holder.btnReceive.setVisibility(View.GONE);
        holder.btnReview.setVisibility(View.GONE);

        // 🌟 核心修复：完善了所有的状态扭转树
        if (o.getStatus() != null) {
            if (o.getStatus() == 0) {
                holder.tvStatus.setText("正在处理 (待发货)");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));

            } else if (o.getStatus() == 1) {
                holder.tvStatus.setText("🚚 运输中 (已发货)");
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
                // 也可以允许用户在这个阶段提前确认收货
                holder.btnReceive.setVisibility(View.VISIBLE);

            } else if (o.getStatus() == 4) { // 🌟 补上了这个至关重要的缺失状态 4
                holder.tvStatus.setText("📦 已到货 (请联系团长提货)");
                holder.tvStatus.setTextColor(Color.parseColor("#9C27B0")); // 醒目的紫色
                // 货到了，允许用户自己点收货（或者让团长扫码核销）
                holder.btnReceive.setVisibility(View.VISIBLE);

            } else if (o.getStatus() == 2) {
                holder.tvStatus.setText("交易完成 (待评价)");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.btnReview.setVisibility(View.VISIBLE);

            } else if (o.getStatus() == 3) {
                holder.tvStatus.setText("已评价 (订单结束)");
                holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
            }
        }

        holder.btnReceive.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            OrderVO currentOrder = orderList.get(currentPos);
            holder.btnReceive.setEnabled(false);

            RetrofitClient.getApi().receiveOrder(currentOrder.getId()).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(v.getContext(), "收货成功，快去评价商品吧！", Toast.LENGTH_SHORT).show();
                        currentOrder.setStatus(2);
                        notifyItemChanged(currentPos);
                    }
                }
                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    holder.btnReceive.setEnabled(true);
                }
            });
        });

        holder.btnReview.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            OrderVO currentOrder = orderList.get(currentPos);

            Intent intent = new Intent(v.getContext(), ReviewActivity.class);
            intent.putExtra("ORDER_ID", currentOrder.getId());
            intent.putExtra("PRODUCT_ID", currentOrder.getProductId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatus, tvTime, tvAmount;
        LinearLayout layoutOrderItems;
        Button btnReceive, btnReview;

        public ViewHolder(View view) {
            super(view);
            tvOrderNo = view.findViewById(R.id.tv_order_no);
            tvStatus = view.findViewById(R.id.tv_order_status);
            tvTime = view.findViewById(R.id.tv_order_time);
            tvAmount = view.findViewById(R.id.tv_order_amount);
            layoutOrderItems = view.findViewById(R.id.layout_order_items);
            btnReceive = view.findViewById(R.id.btn_receive);
            btnReview = view.findViewById(R.id.btn_review);
        }
    }
}