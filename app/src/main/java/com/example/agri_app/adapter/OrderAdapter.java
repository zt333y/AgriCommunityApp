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
        // 🌟 后端修改过 Jackson 属性后，这里直接显示干净利落的时间，再也没有 T 了！
        holder.tvTime.setText("下单时间: " + o.getCreateTime());
        holder.tvAmount.setText("实付: ￥" + o.getTotalAmount());

        // 🌟 这一段是渲染商品明细的核心！
        holder.layoutOrderItems.removeAllViews(); // 先清空，防止列表滑动时数据错乱
        if (o.getItems() != null && !o.getItems().isEmpty()) {
            holder.layoutOrderItems.setVisibility(View.VISIBLE);
            for (OrderItem item : o.getItems()) {
                // 创建一个横向的布局放单个商品
                LinearLayout itemLayout = new LinearLayout(holder.itemView.getContext());
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(0, 10, 0, 10);
                itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

                // 1. 生成 1:1 的商品图片
                ImageView iv = new ImageView(holder.itemView.getContext());
                int imgSize = (int) (50 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
                iv.setLayoutParams(new LinearLayout.LayoutParams(imgSize, imgSize));
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                String imgUrl = item.getImageUrl();
                // 🌟🌟🌟 终极修复：使用暴力截断旧 IP，强行拼接当前绝对正确的 IP！
                if (imgUrl != null && imgUrl.contains("/uploads/")) {
                    imgUrl = "http://192.168.31.60:8080" + imgUrl.substring(imgUrl.indexOf("/uploads/"));
                }

                Glide.with(holder.itemView.getContext())
                        .load(imgUrl)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(iv);

                // 2. 生成商品名称
                TextView tvName = new TextView(holder.itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                params.setMargins(20, 0, 0, 0);
                tvName.setLayoutParams(params);
                tvName.setText(item.getProductName());
                tvName.setTextColor(Color.parseColor("#333333"));
                tvName.setTextSize(14);

                // 3. 生成商品数量
                TextView tvQty = new TextView(holder.itemView.getContext());
                tvQty.setText("x" + item.getQuantity());
                tvQty.setTextColor(Color.parseColor("#666666"));
                tvQty.setTextSize(14);

                // 把图、名、数量组装起来塞进空盒子里
                itemLayout.addView(iv);
                itemLayout.addView(tvName);
                itemLayout.addView(tvQty);
                holder.layoutOrderItems.addView(itemLayout);
            }
        } else {
            holder.layoutOrderItems.setVisibility(View.GONE);
        }

        holder.btnReceive.setVisibility(View.GONE);
        holder.btnReview.setVisibility(View.GONE);

        if (o.getStatus() != null) {
            if (o.getStatus() == 0) {
                holder.tvStatus.setText("正在处理 (待发货)");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            } else if (o.getStatus() == 1) {
                holder.tvStatus.setText("运输中 (已发货)");
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
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
                public void onFailure(Call<Result<String>> call, Throwable t) {}
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
        LinearLayout layoutOrderItems; // 🌟 新绑定的容器
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