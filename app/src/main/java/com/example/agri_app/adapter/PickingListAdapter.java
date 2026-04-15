package com.example.agri_app.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agri_app.R;
import com.example.agri_app.entity.FarmerPickingVO;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickingListAdapter extends RecyclerView.Adapter<PickingListAdapter.ViewHolder> {
    private List<FarmerPickingVO> list;

    public PickingListAdapter(List<FarmerPickingVO> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FarmerPickingVO item = list.get(position);
        holder.tvName.setText(item.getProductName());
        holder.tvQuantity.setText("需采摘: " + item.getTotalQuantity() + " " + item.getUnit());

        // 🌟 核心业务：一键发货逻辑
        holder.btnDone.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("发货确认")
                    .setMessage("确定已采摘完毕？这将会把包含【" + item.getProductName() + "】的所有待发货订单标记为已发货，并通知团长接收。")
                    .setPositiveButton("确定发货", (dialog, which) -> {
                        // 发起网络请求
                        RetrofitClient.getApi().shipByProduct(item.getProductId()).enqueue(new Callback<Result<String>>() {
                            @Override
                            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                                if (response.body() != null && response.body().code == 200) {
                                    Toast.makeText(v.getContext(), "✅ 发货成功！已流转至社区团长", Toast.LENGTH_SHORT).show();
                                    // 从列表中移除该项，更新 UI
                                    int currentPos = holder.getAdapterPosition();
                                    list.remove(currentPos);
                                    notifyItemRemoved(currentPos);
                                    notifyItemRangeChanged(currentPos, list.size());
                                } else {
                                    Toast.makeText(v.getContext(), "发货失败，请重试", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Result<String>> call, Throwable t) {
                                Toast.makeText(v.getContext(), "网络异常", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity;
        Button btnDone;

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_picking_product_name);
            tvQuantity = view.findViewById(R.id.tv_picking_quantity);
            // 🌟 核心：在这里绑定 ID
            btnDone = view.findViewById(R.id.btn_picking_done);
        }
    }
}