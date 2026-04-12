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
import com.example.agri_app.entity.OrderVO;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupLeaderOrderAdapter extends RecyclerView.Adapter<GroupLeaderOrderAdapter.ViewHolder> {
    private List<OrderVO> orderList;

    public GroupLeaderOrderAdapter(List<OrderVO> list) { this.orderList = list; }

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
        holder.btnAction.setVisibility(View.GONE);

        // 🌟 核心状态机逻辑：判断订单当前处于什么阶段
        if (o.getStatus() == 1) { // 农户已发货 -> 团长需【入库】
            holder.tvStatus.setText("🚚 运输中");
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.btnAction.setText("确认到货 (入库)");
            holder.btnAction.setBackgroundColor(Color.parseColor("#FF9800"));
            holder.btnAction.setVisibility(View.VISIBLE);

            holder.btnAction.setOnClickListener(v -> handleAction(v, o, 1));

        } else if (o.getStatus() == 4) { // 团长已入库 -> 等待居民【核销提货】
            holder.tvStatus.setText("📦 待居民提货");
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
            holder.btnAction.setText("扫码/手动核销");
            holder.btnAction.setBackgroundColor(Color.parseColor("#4CAF50"));
            holder.btnAction.setVisibility(View.VISIBLE);

            holder.btnAction.setOnClickListener(v -> handleAction(v, o, 4));

        } else if (o.getStatus() >= 2) { // 已经核销完毕
            holder.tvStatus.setText("✅ 已提货核销");
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
        } else {
            holder.tvStatus.setText("等待农户发货");
            holder.tvStatus.setTextColor(Color.parseColor("#999999"));
        }
    }

    private void handleAction(View v, OrderVO o, int currentStatus) {
        int currentPos = orderList.indexOf(o);
        String actionName = currentStatus == 1 ? "入库确认" : "核销提货";

        new AlertDialog.Builder(v.getContext())
                .setTitle("操作确认")
                .setMessage("确定要执行【" + actionName + "】操作吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    Call<Result<String>> call = currentStatus == 1 ?
                            RetrofitClient.getApi().arriveOrder(o.getId()) :
                            RetrofitClient.getApi().verifyOrder(o.getId());

                    call.enqueue(new Callback<Result<String>>() {
                        @Override
                        public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                            if (response.body() != null && response.body().code == 200) {
                                Toast.makeText(v.getContext(), response.body().data, Toast.LENGTH_SHORT).show();
                                o.setStatus(currentStatus == 1 ? 4 : 2); // 状态推进
                                notifyItemChanged(currentPos);
                            }
                        }
                        @Override
                        public void onFailure(Call<Result<String>> call, Throwable t) {}
                    });
                })
                .setNegativeButton("取消", null).show();
    }

    @Override
    public int getItemCount() { return orderList == null ? 0 : orderList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatus;
        Button btnAction;
        public ViewHolder(View view) {
            super(view);
            tvOrderNo = view.findViewById(R.id.tv_leader_order_no);
            tvStatus = view.findViewById(R.id.tv_leader_status);
            btnAction = view.findViewById(R.id.btn_leader_action);
        }
    }
}