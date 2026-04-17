package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // 🌟 引入 Glide 用于修改时回显旧图片
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

    private Uri selectedImageUri = null; // 用户新选中的图片 URI
    private ImageView ivImage;
    private TextView tvImageHint;

    // 🌟 1. 定义固定的下拉选项
    private final String[] CATEGORY_OPTIONS = {"新鲜水果", "有机蔬菜", "肉禽蛋品", "粮油调味", "水产海鲜", "农副加工"};
    private final String[] UNIT_OPTIONS = {"斤", "公斤", "箱", "个", "只", "包", "份"};
    private Spinner spinnerCategory, spinnerUnit;

    // 🌟 2. 用于记录是不是“修改模式”
    private Long editProductId = null;
    private String oldImageUrl = "";

    // 声明并注册相册选择器
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

        // 绑定普通控件
        TextView tvTitle = findViewById(R.id.tv_publish_title);
        EditText etName = findViewById(R.id.et_pub_name);
        EditText etPrice = findViewById(R.id.et_pub_price);
        EditText etStock = findViewById(R.id.et_pub_stock);
        EditText etDesc = findViewById(R.id.et_pub_desc);
        Button btnPublish = findViewById(R.id.btn_publish);

        ivImage = findViewById(R.id.iv_pub_image);
        tvImageHint = findViewById(R.id.tv_image_hint);
        FrameLayout layoutImagePicker = findViewById(R.id.layout_image_picker);

        // 点击相册框
        layoutImagePicker.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // 🌟 3. 初始化下拉框并绑定数据
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerUnit = findViewById(R.id.spinner_unit);
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORY_OPTIONS));
        spinnerUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNIT_OPTIONS));

        // 🌟 4. 检查是不是从“我的商品库”点击进来的（修改模式）
        Intent intent = getIntent();
        if (intent.hasExtra("EDIT_ID")) {
            editProductId = intent.getLongExtra("EDIT_ID", -1);
            tvTitle.setText("✏️ 修改农产品");
            btnPublish.setText("保存修改");

            // 回显文字数据
            etName.setText(intent.getStringExtra("EDIT_NAME"));
            etPrice.setText(intent.getStringExtra("EDIT_PRICE"));
            etStock.setText(intent.getStringExtra("EDIT_STOCK"));
            etDesc.setText(intent.getStringExtra("EDIT_DESC"));
            oldImageUrl = intent.getStringExtra("EDIT_IMAGE");

            // 回显下拉框选中状态
            setSpinnerSelection(spinnerCategory, CATEGORY_OPTIONS, intent.getStringExtra("EDIT_CATEGORY"));
            setSpinnerSelection(spinnerUnit, UNIT_OPTIONS, intent.getStringExtra("EDIT_UNIT"));

            // 回显旧图片
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                Glide.with(this).load(oldImageUrl).into(ivImage);
                tvImageHint.setVisibility(View.GONE);
            }
        }

        btnPublish.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            // 🌟 5. 从下拉框获取选中的分类和单位
            String category = spinnerCategory.getSelectedItem().toString();
            String unit = spinnerUnit.getSelectedItem().toString();

            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "商品名称、价格和库存为必填项", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long farmerId = sp.getLong("userId", 0L);
            if (farmerId == 0) return;

            btnPublish.setText("数据处理中...");
            btnPublish.setEnabled(false);

            // 🌟 6. 核心判断：是直接提交，还是先上传新图片？
            if (editProductId != null && selectedImageUri == null) {
                // 情景A：是在【修改商品】，且【没有选新图】，直接把老图的 URL 提交上去
                submitProduct(farmerId, name, category, priceStr, stockStr, unit, desc, oldImageUrl, btnPublish, true);
            } else {
                // 情景B：是在【发布新商品】，或者【修改时换了新照片】，必须先走上传图片接口！
                if (selectedImageUri == null) {
                    Toast.makeText(this, "⚠️ 请点击虚线框选择一张商品图片！", Toast.LENGTH_SHORT).show();
                    btnPublish.setText(editProductId != null ? "保存修改" : "立即发布");
                    btnPublish.setEnabled(true);
                    return;
                }

                File imageFile = getFileFromUri(selectedImageUri);
                if (imageFile == null) return;

                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

                RetrofitClient.getApi().uploadImage(body).enqueue(new Callback<Result<String>>() {
                    @Override
                    public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                        if (response.body() != null && response.body().code == 200) {
                            // 图片上传成功，拿到新图的 URL，再提交商品数据
                            submitProduct(farmerId, name, category, priceStr, stockStr, unit, desc, response.body().data, btnPublish, editProductId != null);
                        } else {
                            String errorMsg = response.body() != null ? response.body().msg : "服务器拒绝接收";
                            Toast.makeText(PublishActivity.this, "图片上传失败: " + errorMsg, Toast.LENGTH_LONG).show();
                            btnPublish.setText(editProductId != null ? "保存修改" : "立即发布");
                            btnPublish.setEnabled(true);
                        }
                    }
                    @Override
                    public void onFailure(Call<Result<String>> call, Throwable t) {
                        Toast.makeText(PublishActivity.this, "上传异常: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        btnPublish.setEnabled(true);
                    }
                });
            }
        });
    }

    // 辅助方法：让下拉框自动选中之前发布时存的值
    private void setSpinnerSelection(Spinner spinner, String[] options, String value) {
        if (value == null) return;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // 真正把所有数据提交给后端生成商品库记录的方法 (根据 isEdit 决定调哪个接口)
    private void submitProduct(long farmerId, String name, String category, String priceStr, String stockStr, String unit, String desc, String imageUrl, Button btn, boolean isEdit) {
        Product p = new Product();
        if (isEdit) p.setId(editProductId); // 如果是修改，必须附带 ID 过去
        p.setFarmerId(farmerId);
        p.setName(name);
        p.setCategory(category);
        p.setUnit(unit);
        p.setDescription(desc);
        p.setImageUrl(imageUrl);
        p.setPrice(Double.parseDouble(priceStr));
        p.setStock(Integer.parseInt(stockStr));

        // 🌟 动态选择网络接口：修改调 updateProduct，新增调 addProduct
        Call<Result<String>> call = isEdit ? RetrofitClient.getApi().updateProduct(p) : RetrofitClient.getApi().addProduct(p);

        call.enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    Toast.makeText(PublishActivity.this, isEdit ? "🎉 修改成功！" : "🎉 商品发布成功！", Toast.LENGTH_LONG).show();
                    finish(); // 关闭页面，直接回到列表页
                } else {
                    Toast.makeText(PublishActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                    btn.setText(isEdit ? "保存修改" : "立即发布");
                    btn.setEnabled(true);
                }
            }
            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                btn.setEnabled(true);
            }
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