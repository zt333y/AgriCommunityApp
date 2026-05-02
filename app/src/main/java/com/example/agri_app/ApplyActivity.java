package com.example.agri_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.entity.Apply;
import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplyActivity extends AppCompatActivity {

    private Spinner spProvince, spCity, spDistrict;

    // 🌟 完全复用收货地址的高精度地理字典数据
    private String[] provinces = {"四川省", "重庆市", "云南省"};
    private Map<String, String[]> cityMap = new HashMap<>();
    private Map<String, String[]> districtMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);
        setTitle("资质入驻申请");
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        // 1. 初始化级联数据
        initMockData();

        RadioGroup rgRole = findViewById(R.id.rg_role);
        EditText etName = findViewById(R.id.et_real_name);
        EditText etIdCard = findViewById(R.id.et_id_card);
        EditText etAddressDetail = findViewById(R.id.et_apply_address);
        Button btnSubmit = findViewById(R.id.btn_submit_apply);

        // 2. 绑定省市区 Spinner
        spProvince = findViewById(R.id.spinner_province);
        spCity = findViewById(R.id.spinner_city);
        spDistrict = findViewById(R.id.spinner_district);

        setupSpinners();

        btnSubmit.setOnClickListener(v -> {
            String nameStr = etName.getText().toString().trim();
            String idCardStr = etIdCard.getText().toString().trim();
            String detailAddressStr = etAddressDetail.getText().toString().trim();

            if (nameStr.isEmpty() || idCardStr.isEmpty() || detailAddressStr.isEmpty()) {
                Toast.makeText(this, "请填写完整的申请信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🌟 智能拼接为完整地址：省+市+区+详细地址
            String provinceStr = spProvince.getSelectedItem().toString();
            String cityStr = spCity.getSelectedItem().toString();
            String districtStr = spDistrict.getSelectedItem().toString();
            String finalFullAddress = provinceStr + cityStr + districtStr + detailAddressStr;

            SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            long currentUserId = sp.getLong("userId", 0L);
            if (currentUserId == 0L) return;

            Apply apply = new Apply();
            apply.setUserId(currentUserId);
            apply.setRealName(nameStr);
            apply.setIdCard(idCardStr);
            apply.setAddress(finalFullAddress);

            int selectedRole = (rgRole.getCheckedRadioButtonId() == R.id.rb_farmer) ? 1 : 2;
            apply.setApplyRole(selectedRole);

            btnSubmit.setText("正在提交...");
            btnSubmit.setEnabled(false);

            RetrofitClient.getApi().submitApply(apply).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null && response.body().code == 200) {
                        Toast.makeText(ApplyActivity.this, "申请已提交，请等待管理员审核", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ApplyActivity.this, "提交失败: " + (response.body() != null ? response.body().msg : ""), Toast.LENGTH_SHORT).show();
                        btnSubmit.setText("提交入驻申请");
                        btnSubmit.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    Toast.makeText(ApplyActivity.this, "网络异常，请检查网络", Toast.LENGTH_SHORT).show();
                    btnSubmit.setText("提交入驻申请");
                    btnSubmit.setEnabled(true);
                }
            });
        });
    }

    // ==========================================
    // 🌟 设置下拉框联动的逻辑 (与收货地址完全一致)
    // ==========================================
    private void setupSpinners() {
        // 配置省份下拉框
        ArrayAdapter<String> provAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, provinces);
        spProvince.setAdapter(provAdapter);

        // 原生联动逻辑：当省份改变时，更新城市下拉框
        spProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProv = provinces[position];
                String[] cities = cityMap.get(selectedProv);
                if (cities == null) cities = new String[]{"暂无数据"};

                ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(ApplyActivity.this, android.R.layout.simple_spinner_dropdown_item, cities);
                spCity.setAdapter(cityAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 原生联动逻辑：当城市改变时，更新区县下拉框
        spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = (String) spCity.getSelectedItem();
                String[] districts = districtMap.get(selectedCity);
                if (districts == null) districts = new String[]{"市辖区"};

                ArrayAdapter<String> distAdapter = new ArrayAdapter<>(ApplyActivity.this, android.R.layout.simple_spinner_dropdown_item, districts);
                spDistrict.setAdapter(distAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ==========================================
    // 🌟 初始化原生地理字典 (精准且完整的答辩数据)
    // ==========================================
    private void initMockData() {
        // 1. 绑定省份与城市
        cityMap.put("四川省", new String[]{"成都市", "绵阳市", "德阳市", "南充市", "宜宾市", "泸州市"});
        cityMap.put("重庆市", new String[]{"重庆市"});
        cityMap.put("云南省", new String[]{"昆明市", "曲靖市"});

        // 2. 绑定城市与区县
        // ================= 【四川省区县】 =================
        districtMap.put("成都市", new String[]{
                "锦江区", "青羊区", "金牛区", "武侯区", "成华区", "龙泉驿区", "青白江区",
                "新都区", "温江区", "双流区", "郫都区", "新津区",
                "金堂县", "大邑县", "蒲江县",
                "都江堰市", "彭州市", "邛崃市", "崇州市", "简阳市"
        });

        districtMap.put("绵阳市", new String[]{
                "涪城区", "游仙区", "安州区",
                "三台县", "盐亭县", "梓潼县", "平武县",
                "北川羌族自治县",
                "江油市"
        });

        districtMap.put("德阳市", new String[]{
                "旌阳区", "罗江区",
                "中江县",
                "广汉市", "什邡市", "绵竹市"
        });

        districtMap.put("南充市", new String[]{"顺庆区", "高坪区", "嘉陵区", "南部县", "营山县", "仪陇县", "西充县", "阆中市"});
        districtMap.put("宜宾市", new String[]{"翠屏区", "南溪区", "叙州区", "江安县", "长宁县", "高县", "筠连县", "珙县", "兴文县", "屏山县"});
        districtMap.put("泸州市", new String[]{"江阳区", "龙马潭区", "纳溪区", "泸县", "合江县", "叙永县", "古蔺县"});

        // ================= 【重庆市区县】 =================
        districtMap.put("重庆市", new String[]{
                "渝中区", "万州区", "涪陵区", "大渡口区", "江北区", "沙坪坝区", "九龙坡区", "南岸区", "北碚区", "綦江区",
                "大足区", "渝北区", "巴南区", "黔江区", "长寿区", "江津区", "合川区", "永川区", "南川区", "璧山区",
                "铜梁区", "潼南区", "荣昌区", "开州区", "梁平区", "武隆区",
                "城口县", "丰都县", "垫江县", "忠县", "云阳县", "奉节县", "巫山县", "巫溪县",
                "石柱土家族自治县", "秀山土家族苗族自治县", "酉阳土家族苗族自治县", "彭水苗族土家族自治县"
        });

        // ================= 【云南省区县】 =================
        districtMap.put("昆明市", new String[]{
                "五华区", "盘龙区", "官渡区", "西山区", "东川区", "呈贡区", "晋宁区",
                "富民县", "宜良县", "嵩明县", "石林彝族自治县", "禄劝彝族苗族自治县", "寻甸回族彝族自治县", "安宁市"
        });
        districtMap.put("曲靖市", new String[]{
                "麒麟区", "沾益区", "马龙区",
                "陆良县", "师宗县", "罗平县", "富源县", "会泽县", "宣威市"
        });
    }
}