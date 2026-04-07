package com.example.agri_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agri_app.R;
import com.example.agri_app.entity.CartVO;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartVO> cartList;

    public CartAdapter(List<CartVO> list) {
        this.cartList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartVO c = cartList.get(position);
        // ✅ 修复：强制使用 Get 方法获取私有数据
        holder.tvName.setText(c.getProductName());
        holder.tvPrice.setText("￥" + c.getPrice());
        holder.tvQuantity.setText("x " + c.getQuantity());
    }

    @Override
    public int getItemCount() {
        return cartList == null ? 0 : cartList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        public ViewHolder(View view) {
            super(view);
            // ✅ 把这里的 ID 换回你最初在 XML 里写的名字
            tvName = view.findViewById(R.id.cart_item_name);
            tvPrice = view.findViewById(R.id.cart_item_price);
            tvQuantity = view.findViewById(R.id.cart_item_quantity);
        }
    }
}