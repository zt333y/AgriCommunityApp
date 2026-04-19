package com.example.agri_app.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agri_app.R;
import com.example.agri_app.ReviewActivity;
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

        // 🌟 每次刷新先将按钮默认隐藏，防止 RecyclerView 复用机制导致的显示错乱
        holder.btnReceive.setVisibility(View.GONE);
        holder.btnReview.setVisibility(View.GONE);

        // 🌟 根据订单状态 (0:待发货, 1:已发货, 2:交易完成/待评价, 3:已评价) 动态显示不同按钮和文字
        if (o.getStatus() != null) {
            if (o.getStatus() == 0) {
                holder.tvStatus.setText("正在处理 (待发货)");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // 橙色

            } else if (o.getStatus() == 1) {
                holder.tvStatus.setText("运输中 (已发货)");
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3")); // 蓝色
                holder.btnReceive.setVisibility(View.VISIBLE); // 显示“确认收货”按钮

            } else if (o.getStatus() == 2) {
                holder.tvStatus.setText("交易完成 (待评价)");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // 绿色
                holder.btnReview.setVisibility(View.VISIBLE); // 显示“评价订单”按钮

            } else if (o.getStatus() == 3) {
                holder.tvStatus.setText("已评价 (订单结束)");
                holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E")); // 灰色
            }
        } else {
            holder.tvStatus.setText("状态未知");
        }

        // ==========================================
        // 🌟 逻辑 1：点击【确认收货】
        // ==========================================
        holder.btnReceive.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            OrderVO currentOrder = orderList.get(currentPos);

            // 禁用按钮防连点
            holder.btnReceive.setEnabled(false);

            RetrofitClient.getApi().receiveOrder(currentOrder.getId()).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(v.getContext(), "收货成功，快去评价商品吧！", Toast.LENGTH_SHORT).show();
                        currentOrder.setStatus(2); // 状态变为 2 (待评价)
                        notifyItemChanged(currentPos); // 触发这一个卡片的局部刷新
                    } else {
                        Toast.makeText(v.getContext(), "操作失败", Toast.LENGTH_SHORT).show();
                        holder.btnReceive.setEnabled(true);
                    }
                }
                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(v.getContext(), "网络异常", Toast.LENGTH_SHORT).show();
                    holder.btnReceive.setEnabled(true);
                }
            });
        });

        // ==========================================
        // 🌟 逻辑 2：点击【评价订单】
        // ==========================================
        holder.btnReview.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            OrderVO currentOrder = orderList.get(currentPos);

            // 跳转到评价页面，携带真实的订单ID和真实的商品ID
            Intent intent = new Intent(v.getContext(), ReviewActivity.class);
            intent.putExtra("ORDER_ID", currentOrder.getId());

            // 🌟 核心修复：获取真实的商品 ID，彻底告别写死 "1L"！
            // 备注：如果你的 OrderVO 包含子订单列表，可能需要写成 currentOrder.getItems().get(0).getProductId()
            // 这里按照你的简化版实体类直接取 productId
            intent.putExtra("PRODUCT_ID", currentOrder.getProductId());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    // 绑定所有的控件
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatus, tvTime, tvAmount;
        Button btnReceive, btnReview;

        public ViewHolder(View view) {
            super(view);
            tvOrderNo = view.findViewById(R.id.tv_order_no);
            tvStatus = view.findViewById(R.id.tv_order_status);
            tvTime = view.findViewById(R.id.tv_order_time);
            tvAmount = view.findViewById(R.id.tv_order_amount);

            btnReceive = view.findViewById(R.id.btn_receive);
            btnReview = view.findViewById(R.id.btn_review);
        }
    }
}