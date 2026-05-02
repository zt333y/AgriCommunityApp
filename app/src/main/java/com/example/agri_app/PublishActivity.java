package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
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

    private Uri selectedImageUri = null;
    private ImageView ivImage;
    private View tvImageHint;
    private TextView tvChangeHint; // 🌟 声明新增的更换提示控件

    private final String[] CATEGORY_OPTIONS = {"新鲜水果", "有机蔬菜", "肉禽蛋品", "粮油调味", "水产海鲜", "农副加工"};
    private final String[] UNIT_OPTIONS = {"斤", "公斤", "箱", "个", "只", "包", "份"};
    private Spinner spinnerCategory, spinnerUnit;

    private Long editProductId = null;
    private String oldImageUrl = "";

    // 选择图片的回调
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivImage.setImageURI(uri);

                    tvImageHint.setVisibility(View.GONE); // 隐藏居中的大提示

                    // 🌟 用户选完新图片后，显示底部的半透明更换提示
                    if (tvChangeHint != null) {
                        tvChangeHint.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tv_publish_title);
        EditText etName = findViewById(R.id.et_pub_name);
        EditText etPrice = findViewById(R.id.et_pub_price);
        EditText etStock = findViewById(R.id.et_pub_stock);
        EditText etDesc = findViewById(R.id.et_pub_desc);
        Button btnPublish = findViewById(R.id.btn_publish);

        ivImage = findViewById(R.id.iv_pub_image);
        tvImageHint = findViewById(R.id.tv_image_hint);
        tvChangeHint = findViewById(R.id.tv_change_hint); // 🌟 绑定控件
        CardView layoutImagePicker = findViewById(R.id.layout_image_picker);

        // 点击卡片触发图片选择器
        layoutImagePicker.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerUnit = findViewById(R.id.spinner_unit);
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORY_OPTIONS));
        spinnerUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNIT_OPTIONS));

        // 判断是否是“修改农产品”的入口
        Intent intent = getIntent();
        if (intent.hasExtra("EDIT_ID")) {
            editProductId = intent.getLongExtra("EDIT_ID", -1);
            tvTitle.setText("修改农产品");
            btnPublish.setText("保存修改");

            etName.setText(intent.getStringExtra("EDIT_NAME"));
            etPrice.setText(intent.getStringExtra("EDIT_PRICE"));
            etStock.setText(intent.getStringExtra("EDIT_STOCK"));
            etDesc.setText(intent.getStringExtra("EDIT_DESC"));
            oldImageUrl = intent.getStringExtra("EDIT_IMAGE");

            setSpinnerSelection(spinnerCategory, CATEGORY_OPTIONS, intent.getStringExtra("EDIT_CATEGORY"));
            setSpinnerSelection(spinnerUnit, UNIT_OPTIONS, intent.getStringExtra("EDIT_UNIT"));

            // 🌟🌟 核心修复：加载原图时，强制拼接上后端的真实 IP 地址
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                String displayUrl = oldImageUrl;

                // 如果是残缺的相对路径，补全为服务器真实地址
                if (displayUrl.contains("/uploads/")) {
                    displayUrl = "http://192.168.31.60:8080" + displayUrl.substring(displayUrl.indexOf("/uploads/"));
                }

                Glide.with(this)
                        .load(displayUrl)
                        .placeholder(R.mipmap.ic_launcher) // 加入占位图防崩溃
                        .into(ivImage);

                tvImageHint.setVisibility(View.GONE); // 隐藏居中的大提示

                // 核心：显示底部半透明更换提示
                if (tvChangeHint != null) {
                    tvChangeHint.setVisibility(View.VISIBLE);
                }
            }
        }

        // 发布/保存按钮逻辑
        btnPublish.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
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

            // 🌟 核心保留：如果是修改商品，且没有选新图片，就直接提交原图的 oldImageUrl
            if (editProductId != null && selectedImageUri == null) {
                submitProduct(farmerId, name, category, priceStr, stockStr, unit, desc, oldImageUrl, btnPublish, true);
            } else {
                if (selectedImageUri == null) {
                    Toast.makeText(this, "请点击虚线框选择一张商品图片！", Toast.LENGTH_SHORT).show();
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
                            submitProduct(farmerId, name, category, priceStr, stockStr, unit, desc, response.body().data, btnPublish, editProductId != null);
                        } else {
                            Toast.makeText(PublishActivity.this, "图片上传失败", Toast.LENGTH_LONG).show();
                            btnPublish.setText(editProductId != null ? "保存修改" : "立即发布");
                            btnPublish.setEnabled(true);
                        }
                    }
                    @Override
                    public void onFailure(Call<Result<String>> call, Throwable t) {
                        btnPublish.setEnabled(true);
                    }
                });
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String[] options, String value) {
        if (value == null) return;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void submitProduct(long farmerId, String name, String category, String priceStr, String stockStr, String unit, String desc, String imageUrl, Button btn, boolean isEdit) {
        Product p = new Product();
        if (isEdit) p.setId(editProductId);
        p.setFarmerId(farmerId);
        p.setName(name);
        p.setCategory(category);
        p.setUnit(unit);
        p.setDescription(desc);
        p.setImageUrl(imageUrl);
        p.setPrice(Double.parseDouble(priceStr));
        p.setStock(Integer.parseInt(stockStr));

        Call<Result<String>> call = isEdit ? RetrofitClient.getApi().updateProduct(p) : RetrofitClient.getApi().addProduct(p);

        call.enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    Toast.makeText(PublishActivity.this, isEdit ? "修改成功" : "商品发布成功", Toast.LENGTH_LONG).show();
                    finish();
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