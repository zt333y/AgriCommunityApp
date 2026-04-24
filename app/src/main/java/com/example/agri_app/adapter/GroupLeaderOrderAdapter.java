package com.example.agri_app.adapter;

import android.app.AlertDialog;
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
import com.example.agri_app.entity.OrderItem;
import com.example.agri_app.entity.OrderVO;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupLeaderOrderAdapter extends RecyclerView.Adapter<GroupLeaderOrderAdapter.ViewHolder> {
    private List<OrderVO> orderList;

    public GroupLeaderOrderAdapter(List<OrderVO> list) {
        this.orderList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leader_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderVO o = orderList.get(position);
        holder.tvOrderNo.setText("提单号: " + o.getOrderNo());

        // 🌟 动态拼接并显示商品明细
        if (o.getItems() != null && !o.getItems().isEmpty()) {
            StringBuilder detailBuilder = new StringBuilder();
            for (OrderItem item : o.getItems()) {
                detailBuilder.append("▪ ")
                        .append(item.getProductName())
                        .append("  x")
                        .append(item.getQuantity())
                        .append("\n");
            }
            holder.tvDetails.setText(detailBuilder.toString().trim());
        } else {
            holder.tvDetails.setText("无商品明细");
        }

        holder.btnAction.setVisibility(View.GONE);

        if (o.getStatus() == 1) {
            holder.tvStatus.setText("🚚 运输中 (需入库)");
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.btnAction.setText("确认到货 (入库)");
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setOnClickListener(v -> handleAction(v, o, 1));

        } else if (o.getStatus() == 4) {
            holder.tvStatus.setText("📦 待居民提货");
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
            holder.btnAction.setText("扫码/手动核销");
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setOnClickListener(v -> handleAction(v, o, 4));

        } else if (o.getStatus() >= 2) {
            holder.tvStatus.setText("✅ 已提货核销");
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
        } else {
            holder.tvStatus.setText("状态流转中...");
            holder.tvStatus.setTextColor(Color.parseColor("#999999"));
        }
    }

    private void handleAction(View v, OrderVO o, int currentStatus) {
        int currentPos = holderPosition(o);
        if (currentPos == -1) return;

        String actionName = currentStatus == 1 ? "到货签收" : "核销出库";

        new AlertDialog.Builder(v.getContext())
                .setTitle("操作确认")
                .setMessage("确定要执行【" + actionName + "】吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    Call<Result<String>> call = currentStatus == 1 ?
                            RetrofitClient.getApi().arriveOrder(o.getId()) :
                            RetrofitClient.getApi().verifyOrder(o.getId());

                    call.enqueue(new Callback<Result<String>>() {
                        @Override
                        public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                            if (response.body() != null && response.body().code == 200) {
                                Toast.makeText(v.getContext(), "操作成功！", Toast.LENGTH_SHORT).show();
                                o.setStatus(currentStatus == 1 ? 4 : 2);
                                notifyItemChanged(currentPos);
                            }
                        }
                        @Override
                        public void onFailure(Call<Result<String>> call, Throwable t) {}
                    });
                })
                .setNegativeButton("取消", null).show();
    }

    private int holderPosition(OrderVO o) {
        return orderList.indexOf(o);
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatus, tvDetails; // 🌟 声明 tvDetails
        Button btnAction;
        public ViewHolder(View view) {
            super(view);
            tvOrderNo = view.findViewById(R.id.tv_leader_order_no);
            tvStatus = view.findViewById(R.id.tv_leader_status);
            tvDetails = view.findViewById(R.id.tv_order_details_list); // 🌟 绑定控件
            btnAction = view.findViewById(R.id.btn_leader_action);
        }
    }
}