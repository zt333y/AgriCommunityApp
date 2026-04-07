package com.example.agri_app.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agri_app.R;
import com.example.agri_app.entity.OrderVO;
import java.util.List;

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

        // 根据状态显示不同文字和颜色 (0:待处理, 1:已发货, 2:已完成)
        if (o.getStatus() != null) {
            if (o.getStatus() == 0) {
                holder.tvStatus.setText("正在处理");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // 橙色
            } else if (o.getStatus() == 1) {
                holder.tvStatus.setText("已发货");
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3")); // 蓝色
            } else {
                holder.tvStatus.setText("交易完成");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // 绿色
            }
        } else {
            holder.tvStatus.setText("状态未知");
        }
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatus, tvTime, tvAmount;
        public ViewHolder(View view) {
            super(view);
            tvOrderNo = view.findViewById(R.id.tv_order_no);
            tvStatus = view.findViewById(R.id.tv_order_status);
            tvTime = view.findViewById(R.id.tv_order_time);
            tvAmount = view.findViewById(R.id.tv_order_amount);
        }
    }
}