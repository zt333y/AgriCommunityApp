package com.example.agri_app.adapter;

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

    public OrderAdapter(List<OrderVO> list) { this.orderList = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderVO o = orderList.get(position);
        holder.tvNo.setText(o.orderNo);
        holder.tvProducts.setText(o.productNames);
        holder.tvPrice.setText("实付款：￥" + o.totalAmount);

        // 判断状态
        if (o.status != null && o.status == 1) {
            holder.tvStatus.setText("待发货");
        } else {
            holder.tvStatus.setText("其他状态");
        }
    }

    @Override
    public int getItemCount() { return orderList == null ? 0 : orderList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNo, tvProducts, tvPrice, tvStatus;
        public ViewHolder(View view) {
            super(view);
            tvNo = view.findViewById(R.id.tv_order_no);
            tvProducts = view.findViewById(R.id.tv_order_products);
            tvPrice = view.findViewById(R.id.tv_order_price);
            tvStatus = view.findViewById(R.id.tv_order_status);
        }
    }
}