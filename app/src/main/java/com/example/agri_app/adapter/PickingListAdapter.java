package com.example.agri_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agri_app.R;
import com.example.agri_app.entity.FarmerPickingVO;
import java.util.List;

public class PickingListAdapter extends RecyclerView.Adapter<PickingListAdapter.ViewHolder> {
    private List<FarmerPickingVO> list;

    public PickingListAdapter(List<FarmerPickingVO> list) { this.list = list; }

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
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity;
        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_picking_product_name);
            tvQuantity = view.findViewById(R.id.tv_picking_quantity);
        }
    }
}