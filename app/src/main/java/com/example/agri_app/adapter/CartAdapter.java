package com.example.agri_app.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agri_app.ProductDetailActivity;
import com.example.agri_app.R;
import com.example.agri_app.entity.CartVO;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartVO> cartList;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public CartAdapter(List<CartVO> list, OnCartChangeListener listener) {
        this.cartList = list;
        this.listener = listener;
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
        holder.tvQuantity.setText(String.valueOf(c.getQuantity()));

        // 图片渲染
        if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
            String imageUrl = c.getImageUrl();
            // 🌟 换成这段终极截断拼接法
            if (imageUrl.contains("/uploads/")) {
                imageUrl = "http://192.168.31.60:8080" + imageUrl.substring(imageUrl.indexOf("/uploads/"));
            }
            Glide.with(holder.itemView.getContext()).load(imageUrl).placeholder(R.mipmap.ic_launcher).into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.mipmap.ic_launcher);
        }

// 🌟 核心修复：完全模仿首页的传参方式！大写字母必须对齐！
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);

            intent.putExtra("ID", c.getProductId());      // 传商品ID给详情页加载评价
            intent.putExtra("NAME", c.getProductName());  // 传商品名字
            intent.putExtra("PRICE", c.getPrice());       // 传商品价格
            intent.putExtra("IMAGE_URL", c.getImageUrl());// 传商品图片
            intent.putExtra("DESC", "优质农产品");         // 购物车接口里没有长描述，给个占位符兜底

            v.getContext().startActivity(intent);
        });

        // 加号点击 (🌟 修复：使用 getId() 而不是 getCartId())
        holder.btnPlus.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            int newQuantity = cartList.get(currentPos).getQuantity() + 1;
            updateQuantityToServer(cartList.get(currentPos).getId(), newQuantity, currentPos);
        });

        // 减号点击 (🌟 修复：使用 getId())
        holder.btnMinus.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            if (cartList.get(currentPos).getQuantity() > 1) {
                int newQuantity = cartList.get(currentPos).getQuantity() - 1;
                updateQuantityToServer(cartList.get(currentPos).getId(), newQuantity, currentPos);
            } else {
                Toast.makeText(v.getContext(), "不能再少啦！", Toast.LENGTH_SHORT).show();
            }
        });

        // 删除按钮点击 (🌟 修复：使用 getId())
        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            RetrofitClient.getApi().deleteCartItem(cartList.get(currentPos).getId()).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    cartList.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    notifyItemRangeChanged(currentPos, cartList.size());
                    listener.onCartChanged(); // 通知父页面算钱
                }
                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {}
            });
        });
    }

    private void updateQuantityToServer(Long cartId, int newQuantity, int position) {
        RetrofitClient.getApi().updateCartQuantity(cartId, newQuantity).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    cartList.get(position).setQuantity(newQuantity);
                    notifyItemChanged(position); // 局部刷新该行的 UI
                    listener.onCartChanged(); // 通知父页面算钱
                }
            }
            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {}
        });
    }

    @Override
    public int getItemCount() {
        return cartList == null ? 0 : cartList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity, btnMinus, btnPlus;
        ImageView ivImage, btnDelete;

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.cart_item_name);
            tvPrice = view.findViewById(R.id.cart_item_price);
            tvQuantity = view.findViewById(R.id.cart_item_quantity);
            ivImage = view.findViewById(R.id.cart_item_image);
            btnMinus = view.findViewById(R.id.btn_minus);
            btnPlus = view.findViewById(R.id.btn_plus);
            btnDelete = view.findViewById(R.id.btn_delete_cart);
        }
    }
}