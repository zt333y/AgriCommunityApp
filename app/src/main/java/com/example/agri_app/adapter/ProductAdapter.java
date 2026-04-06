package com.example.agri_app.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agri_app.R;
import com.example.agri_app.entity.Product;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> productList;

    public ProductAdapter(List<Product> list) {
        this.productList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = productList.get(position);
        holder.tvName.setText(p.name);
        holder.tvDesc.setText(p.description);
        holder.tvPrice.setText("￥" + p.price);

        String testImageUrl = "https://images.unsplash.com/photo-1610832958506-aa56368176cf?auto=format&fit=crop&w=300&q=80"; // 苹果的高清网图

        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(testImageUrl)
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            // 创建一个 Intent 准备跳转到 ProductDetailActivity
            android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.agri_app.ProductDetailActivity.class);

            // 把当前点击的商品信息“塞”进 Intent 里带过去
            intent.putExtra("ID", p.id);
            intent.putExtra("NAME", p.name);
            intent.putExtra("PRICE", p.price);
            intent.putExtra("DESC", p.description);
            intent.putExtra("IMAGE_URL", testImageUrl);
            // 开始跳转！
            v.getContext().startActivity(intent);
        });
        // 👆 修改结束 👆
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice;
        android.widget.ImageView ivImage;
        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_name);
            tvDesc = view.findViewById(R.id.tv_desc);
            tvPrice = view.findViewById(R.id.tv_price);
            ivImage = view.findViewById(R.id.iv_product_image);
        }
    }
}