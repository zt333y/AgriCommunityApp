package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.entity.Product;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishActivity extends AppCompatActivity {

    private Uri selectedImageUri = null; // 用户选中的图片 URI
    private ImageView ivImage;
    private TextView tvImageHint;

    // 🌟 1. 声明并注册 Android 官方最新的图片选择器 (免去了恶心的权限申请)
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri; // 记录真实图片路径
                    ivImage.setImageURI(uri); // 预览显示图片
                    tvImageHint.setVisibility(View.GONE); // 隐藏提示文字
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        // 绑定控件
        EditText etName = findViewById(R.id.et_pub_name);
        EditText etCategory = findViewById(R.id.et_pub_category);
        EditText etPrice = findViewById(R.id.et_pub_price);
        EditText etStock = findViewById(R.id.et_pub_stock);
        EditText etUnit = findViewById(R.id.et_pub_unit);
        EditText etDesc = findViewById(R.id.et_pub_desc);
        Button btnPublish = findViewById(R.id.btn_publish);

        ivImage = findViewById(R.id.iv_pub_image);
        tvImageHint = findViewById(R.id.tv_image_hint);
        FrameLayout layoutImagePicker = findViewById(R.id.layout_image_picker);

        // 🌟 2. 点击框框，唤起手机相册！
        layoutImagePicker.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnPublish.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();
            String unit = etUnit.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "商品名称、分类、价格和库存为必填项", Toast.LENGTH_SHORT).show();
                return;
            }

            // 拦截：必须要选图
            if (selectedImageUri == null) {
                Toast.makeText(this, "⚠️ 请点击虚线框选择一张商品图片！", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long farmerId = sp.getLong("userId", 0L);
            if (farmerId == 0) return;

            // 锁定按钮防连点
            btnPublish.setText("照片上传处理中...");
            btnPublish.setEnabled(false);

            // 🌟 3. 将相册里的 URI 转成真实的临时文件
            File imageFile = getFileFromUri(selectedImageUri);
            if (imageFile == null) {
                Toast.makeText(this, "图片处理失败", Toast.LENGTH_SHORT).show();
                btnPublish.setText("立即发布");
                btnPublish.setEnabled(true);
                return;
            }

            // 🌟 4. 构建网络层文件流，先调用文件上传接口！
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            RetrofitClient.getApi().uploadImage(body).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        // 🌟 5. 图片上传成功！拿到后端的图片地址，再去调用真正的发布商品方法
                        String realImageUrl = response.body().data;
                        submitProduct(farmerId, name, category, priceStr, stockStr, unit, desc, realImageUrl, btnPublish);
                    } else {
                        Toast.makeText(PublishActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();
                        btnPublish.setText("立即发布");
                        btnPublish.setEnabled(true);
                    }
                }
                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(PublishActivity.this, "图片上传异常: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPublish.setText("立即发布");
                    btnPublish.setEnabled(true);
                }
            });
        });
    }

    // 真正把所有数据提交给后端生成商品库记录的方法
    private void submitProduct(long farmerId, String name, String category, String priceStr, String stockStr, String unit, String desc, String imageUrl, Button btn) {
        Product newProduct = new Product();
        newProduct.setFarmerId(farmerId);
        newProduct.setName(name);
        newProduct.setCategory(category);
        newProduct.setUnit(unit);
        newProduct.setDescription(desc);
        newProduct.setImageUrl(imageUrl); // 填入刚刚上传成功返回的新鲜 URL
        newProduct.setPrice(Double.parseDouble(priceStr));
        newProduct.setStock(Integer.parseInt(stockStr));

        RetrofitClient.getApi().addProduct(newProduct).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    Toast.makeText(PublishActivity.this, "🎉 商品发布成功！", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(PublishActivity.this, MyProductsActivity.class));
                    finish();
                } else {
                    btn.setText("立即发布");
                    btn.setEnabled(true);
                }
            }
            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {}
        });
    }

    // 神级工具方法：将系统分配的虚拟 URI 转为我们可以上传的真实 File
    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = new File(getCacheDir(), "upload_temp.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}