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

        // 动态拼接并显示商品明细
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

        // 默认隐藏操作按钮，并重置颜色
        holder.btnAction.setVisibility(View.GONE);
        holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));

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

        } else if (o.getStatus() == 2 || o.getStatus() == 3) { // 🌟 拆分>=2的判断
            holder.tvStatus.setText("✅ 已提货核销");
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));

            // ==========================================
            // 🌟🌟🌟 新增：团长端对售后状态的拦截展示 🌟🌟🌟
            // ==========================================
        } else if (o.getStatus() == 5) {
            holder.tvStatus.setText("🔄 用户申请售后中");
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));

        } else if (o.getStatus() == 6) {
            holder.tvStatus.setText("🔙 待用户退回商品");
            holder.tvStatus.setTextColor(Color.parseColor("#E91E63")); // 粉红色警告

            // 🌟 核心：显示收退货按钮
            holder.btnAction.setText("确认收到退货");
            holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E91E63")));
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setOnClickListener(v -> handleAction(v, o, 6)); // 传入状态 6

        } else if (o.getStatus() == 7) {
            holder.tvStatus.setText("✅ 已收退货 (退款中)");
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));

        } else if (o.getStatus() == 8) {
            holder.tvStatus.setText("❌ 售后被拒");
            holder.tvStatus.setTextColor(Color.parseColor("#F44336"));

        } else {
            holder.tvStatus.setText("未发货");
            holder.tvStatus.setTextColor(Color.parseColor("#999999"));
        }
    }

    private void handleAction(View v, OrderVO o, int currentStatus) {
        int currentPos = holderPosition(o);
        if (currentPos == -1) return;

        // 🌟 动态确定对话框文字和要调用的接口
        String actionName;
        Call<Result<String>> call;

        if (currentStatus == 1) {
            actionName = "到货签收";
            call = RetrofitClient.getApi().arriveOrder(o.getId());
        } else if (currentStatus == 4) {
            actionName = "核销出库";
            call = RetrofitClient.getApi().verifyOrder(o.getId());
        } else if (currentStatus == 6) {
            // 🌟 状态6时的专属逻辑
            actionName = "确认收到用户的退货";
            call = RetrofitClient.getApi().leaderConfirmReturn(o.getId());
        } else {
            return;
        }

        new AlertDialog.Builder(v.getContext())
                .setTitle("操作确认")
                .setMessage("确定要执行【" + actionName + "】吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    call.enqueue(new Callback<Result<String>>() {
                        @Override
                        public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                            if (response.body() != null && response.body().code == 200) {
                                Toast.makeText(v.getContext(), "操作成功！", Toast.LENGTH_SHORT).show();
                                // 🌟 状态流转：1变4，4变2，6变7
                                if (currentStatus == 1) o.setStatus(4);
                                else if (currentStatus == 4) o.setStatus(2);
                                else if (currentStatus == 6) o.setStatus(7);

                                notifyItemChanged(currentPos);
                            } else {
                                Toast.makeText(v.getContext(), response.body() != null ? response.body().msg : "操作失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Result<String>> call, Throwable t) {
                            Toast.makeText(v.getContext(), "网络异常", Toast.LENGTH_SHORT).show();
                        }
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
        TextView tvOrderNo, tvStatus, tvDetails;
        Button btnAction;
        public ViewHolder(View view) {
            super(view);
            tvOrderNo = view.findViewById(R.id.tv_leader_order_no);
            tvStatus = view.findViewById(R.id.tv_leader_status);
            tvDetails = view.findViewById(R.id.tv_order_details_list);
            btnAction = view.findViewById(R.id.btn_leader_action);
        }
    }
}