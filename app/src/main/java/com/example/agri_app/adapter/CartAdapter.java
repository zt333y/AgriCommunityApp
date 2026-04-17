package com.example.agri_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // 🌟 引入 Glide 框架加载图片
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
        holder.tvName.setText(c.getProductName());
        holder.tvPrice.setText("￥" + c.getPrice());
        holder.tvQuantity.setText("x " + c.getQuantity());

        // 🌟 核心渲染：把拿到的图片链接画到屏幕上
        if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(c.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher) // 加载过程中的默认图
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.mipmap.ic_launcher); // 兜底
        }
    }

    @Override
    public int getItemCount() {
        return cartList == null ? 0 : cartList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        ImageView ivImage; // 🌟 声明图片控件

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.cart_item_name);
            tvPrice = view.findViewById(R.id.cart_item_price);
            tvQuantity = view.findViewById(R.id.cart_item_quantity);
            // 🌟 绑定 XML 里预留好的那个图片坑位
            ivImage = view.findViewById(R.id.cart_item_image);
        }
    }
}