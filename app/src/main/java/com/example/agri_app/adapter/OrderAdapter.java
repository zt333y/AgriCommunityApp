package com.example.agri_app.adapter;

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

        // 默认先隐藏收货按钮
        holder.btnReceive.setVisibility(View.GONE);

        // 根据状态显示不同文字和颜色 (0:待处理, 1:已发货, 2:已完成)
        if (o.getStatus() != null) {
            if (o.getStatus() == 0) {
                holder.tvStatus.setText("正在处理 (待发货)");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // 橙色
            } else if (o.getStatus() == 1) {
                holder.tvStatus.setText("运输中 (已发货)");
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3")); // 蓝色
                // 🌟 核心：只有已发货的状态，才显示确认收货按钮！
                holder.btnReceive.setVisibility(View.VISIBLE);
            } else {
                holder.tvStatus.setText("交易完成");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // 绿色
            }
        } else {
            holder.tvStatus.setText("状态未知");
        }

        // 🌟 点击确认收货的逻辑
        holder.btnReceive.setOnClickListener(v -> {
            RetrofitClient.getApi().receiveOrder(o.getId()).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(v.getContext(), "收货成功，交易完成！", Toast.LENGTH_SHORT).show();
                        // 修改本地数据状态，并刷新当前这一行
                        o.setStatus(2);
                        notifyItemChanged(position);
                    } else {
                        Toast.makeText(v.getContext(), "操作失败", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(v.getContext(), "网络异常", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatus, tvTime, tvAmount;
        Button btnReceive; // 🌟 声明按钮

        public ViewHolder(View view) {
            super(view);
            tvOrderNo = view.findViewById(R.id.tv_order_no);
            tvStatus = view.findViewById(R.id.tv_order_status);
            tvTime = view.findViewById(R.id.tv_order_time);
            tvAmount = view.findViewById(R.id.tv_order_amount);
            btnReceive = view.findViewById(R.id.btn_receive); // 🌟 绑定按钮
        }
    }
}