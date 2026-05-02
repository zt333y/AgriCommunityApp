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

        holder.tvAmount.setText("￥" + o.getTotalAmount());

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

        // 重置所有的隐藏部件
        holder.btnReceive.setVisibility(View.GONE);
        holder.btnReview.setVisibility(View.GONE);
        holder.btnAfterSales.setVisibility(View.GONE); // 🌟 重置售后按钮隐藏
        holder.layoutLeaderInfo.setVisibility(View.GONE);

        if (o.getStatus() != null) {
            if (o.getStatus() == 0) {
                holder.tvStatus.setText("待发货");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));

            } else if (o.getStatus() == 1) {
                holder.tvStatus.setText("已发货");
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
                holder.btnReceive.setVisibility(View.VISIBLE);

            } else if (o.getStatus() == 4) {
                holder.tvStatus.setText("待提货");
                holder.tvStatus.setTextColor(Color.parseColor("#9C27B0")); // 紫色高亮
                holder.btnReceive.setVisibility(View.VISIBLE);

                holder.layoutLeaderInfo.setVisibility(View.VISIBLE);
                String lName = (o.getLeaderName() != null && !o.getLeaderName().isEmpty()) ? o.getLeaderName() : "未分配";
                String lPhone = (o.getLeaderPhone() != null && !o.getLeaderPhone().isEmpty()) ? o.getLeaderPhone() : "暂无联系方式";
                String lAddress = (o.getPickupAddress() != null && !o.getPickupAddress().isEmpty()) ? o.getPickupAddress() : "请咨询团长具体位置";

                holder.tvLeaderDetails.setText("团长: " + lName + "\n电话: " + lPhone);
                holder.tvPickupAddress.setText("提货地址: " + lAddress);

            } else if (o.getStatus() == 2 || o.getStatus() == 3) {
                // 🌟 修复合并：状态2(待评价)和状态3(已完成)，都进行24小时售后判断

                if (o.getStatus() == 2) {
                    holder.tvStatus.setText("待评价");
                    holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                    holder.btnReview.setVisibility(View.VISIBLE);
                } else {
                    holder.tvStatus.setText("已完成");
                    holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
                }

                // 🌟 核心：计算提货是否在 24 小时内（即便评价了也可以售后）
                if (o.getReceiveTime() != null) {
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                        java.util.Date receiveDate = sdf.parse(o.getReceiveTime().replace("T", " "));
                        if (receiveDate != null) {
                            long diffInMillis = System.currentTimeMillis() - receiveDate.getTime();
                            long hours24 = 24 * 60 * 60 * 1000L;
                            if (diffInMillis <= hours24) {
                                holder.btnAfterSales.setVisibility(View.VISIBLE); // 24小时内显示售后
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else if (o.getStatus() == 5) {
                holder.tvStatus.setText("售后审核中");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));

            } else if (o.getStatus() == 6) {
                holder.tvStatus.setText("待退回团长");
                holder.tvStatus.setTextColor(Color.parseColor("#E91E63"));

                // 审核通过后，依然要显示团长信息，让用户去退货
                holder.layoutLeaderInfo.setVisibility(View.VISIBLE);
                String lName = (o.getLeaderName() != null && !o.getLeaderName().isEmpty()) ? o.getLeaderName() : "未分配";
                String lPhone = (o.getLeaderPhone() != null && !o.getLeaderPhone().isEmpty()) ? o.getLeaderPhone() : "暂无联系方式";
                String lAddress = (o.getPickupAddress() != null && !o.getPickupAddress().isEmpty()) ? o.getPickupAddress() : "请咨询团长具体位置";

                holder.tvLeaderDetails.setText("团长: " + lName + "\n电话: " + lPhone);
                holder.tvPickupAddress.setText("退货地址: " + lAddress); // 改为退货地址

            } else if (o.getStatus() == 7) {
                holder.tvStatus.setText("售后完成");
                holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));

            } else if (o.getStatus() == 8) {
                holder.tvStatus.setText("售后被拒");
                holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
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
                        Toast.makeText(v.getContext(), "收货成功，快去评价商品吧", Toast.LENGTH_SHORT).show();
                        currentOrder.setStatus(2);
                        // 记录本地时间，避免重启前无法申请售后
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                        currentOrder.setReceiveTime(sdf.format(new java.util.Date()));
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

        // 🌟 新增：申请售后按钮点击逻辑
        holder.btnAfterSales.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            OrderVO currentOrder = orderList.get(currentPos);

            // 弹出对话框输入原因
            android.widget.EditText inputReason = new android.widget.EditText(v.getContext());
            inputReason.setHint("请输入退款/退货原因...");
            inputReason.setPadding(40, 40, 40, 40);
            inputReason.setBackgroundResource(android.R.color.transparent);

            new android.app.AlertDialog.Builder(v.getContext())
                    .setTitle("申请售后")
                    .setMessage("提示：提货超过24小时将无法提交售后申请")
                    .setView(inputReason)
                    .setPositiveButton("提交申请", (dialog, which) -> {
                        String reason = inputReason.getText().toString().trim();
                        if (reason.isEmpty()) {
                            Toast.makeText(v.getContext(), "售后原因不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        holder.btnAfterSales.setEnabled(false);
                        RetrofitClient.getApi().applyAfterSales(currentOrder.getId(), reason).enqueue(new Callback<Result<String>>() {
                            @Override
                            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                                if (response.body() != null && response.body().code == 200) {
                                    Toast.makeText(v.getContext(), "申请成功，请等待审核", Toast.LENGTH_SHORT).show();
                                    currentOrder.setStatus(5);
                                    currentOrder.setRefundReason(reason);
                                    notifyItemChanged(currentPos);
                                } else {
                                    Toast.makeText(v.getContext(), response.body() != null ? response.body().msg : "申请失败", Toast.LENGTH_SHORT).show();
                                    holder.btnAfterSales.setEnabled(true);
                                }
                            }
                            @Override
                            public void onFailure(Call<Result<String>> call, Throwable t) {
                                Toast.makeText(v.getContext(), "网络异常，请检查连接", Toast.LENGTH_SHORT).show();
                                holder.btnAfterSales.setEnabled(true);
                            }
                        });
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatus, tvTime, tvAmount;
        LinearLayout layoutOrderItems;
        Button btnReceive, btnReview, btnAfterSales; // 🌟 声明售后按钮

        LinearLayout layoutLeaderInfo;
        TextView tvLeaderDetails;
        TextView tvPickupAddress;

        public ViewHolder(View view) {
            super(view);
            tvOrderNo = view.findViewById(R.id.tv_order_no);
            tvStatus = view.findViewById(R.id.tv_order_status);
            tvTime = view.findViewById(R.id.tv_order_time);
            tvAmount = view.findViewById(R.id.tv_order_amount);
            layoutOrderItems = view.findViewById(R.id.layout_order_items);
            btnReceive = view.findViewById(R.id.btn_receive);
            btnReview = view.findViewById(R.id.btn_review);
            btnAfterSales = view.findViewById(R.id.btn_after_sales); // 🌟 绑定售后按钮

            layoutLeaderInfo = view.findViewById(R.id.layout_leader_info);
            tvLeaderDetails = view.findViewById(R.id.tv_leader_details);
            tvPickupAddress = view.findViewById(R.id.tv_pickup_address);
        }
    }
}