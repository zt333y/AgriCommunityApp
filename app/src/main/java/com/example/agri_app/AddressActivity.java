package com.example.agri_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agri_app.entity.Result;
import com.example.agri_app.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressActivity extends AppCompatActivity {

    private Spinner spProvince, spCity, spDistrict;
    private EditText etDetail;
    private Long userId;

    // 🌟 扩充版省市区数据字典（无任何外部依赖，完全契合答辩演示）
    private String[] provinces = {"四川省", "重庆市", "云南省"};
    private Map<String, String[]> cityMap = new HashMap<>();
    private Map<String, String[]> districtMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        // 1. 初始化级联数据
        initMockData();

        // 2. 绑定控件
        spProvince = findViewById(R.id.spinner_province);
        spCity = findViewById(R.id.spinner_city);
        spDistrict = findViewById(R.id.spinner_district);
        etDetail = findViewById(R.id.et_address_detail);
        Button btnSave = findViewById(R.id.btn_save_address);

        // 3. 配置省份下拉框
        ArrayAdapter<String> provAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, provinces);
        spProvince.setAdapter(provAdapter);

        // 4. 原生联动逻辑：当省份改变时，更新城市下拉框
        spProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProv = provinces[position];
                String[] cities = cityMap.get(selectedProv);
                if (cities == null) cities = new String[]{"暂无数据"};

                ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(AddressActivity.this, android.R.layout.simple_spinner_dropdown_item, cities);
                spCity.setAdapter(cityAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 5. 原生联动逻辑：当城市改变时，更新区县下拉框
        spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = (String) spCity.getSelectedItem();
                String[] districts = districtMap.get(selectedCity);
                if (districts == null) districts = new String[]{"市辖区"};

                ArrayAdapter<String> distAdapter = new ArrayAdapter<>(AddressActivity.this, android.R.layout.simple_spinner_dropdown_item, districts);
                spDistrict.setAdapter(distAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 6. 获取当前登录用户及简单回显逻辑
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sp.getLong("userId", -1L);

        String oldAddr = sp.getString("address", "");
        if (oldAddr.contains("区") || oldAddr.contains("县") || oldAddr.contains("市")) {
            int index = Math.max(oldAddr.lastIndexOf("区"), Math.max(oldAddr.lastIndexOf("县"), oldAddr.lastIndexOf("市")));
            if (index != -1 && index + 1 < oldAddr.length()) {
                etDetail.setText(oldAddr.substring(index + 1));
            }
        } else {
            etDetail.setText(oldAddr);
        }

        // 7. 保存并提交给后端
        btnSave.setOnClickListener(v -> {
            String prov = (String) spProvince.getSelectedItem();
            String city = (String) spCity.getSelectedItem();
            String dist = (String) spDistrict.getSelectedItem();
            String detail = etDetail.getText().toString().trim();

            if (detail.isEmpty()) {
                Toast.makeText(this, "详细门牌号不能为空哦", Toast.LENGTH_SHORT).show();
                return;
            }

            // 智能拼接为完整地址：省+市+区+详细地址
            String finalAddress = prov + city + dist + detail;
            saveAddressToBackend(finalAddress);
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
        // 🌟 成都市 (全 - 12区、3县、代管4市)
        districtMap.put("成都市", new String[]{
                "锦江区", "青羊区", "金牛区", "武侯区", "成华区", "龙泉驿区", "青白江区",
                "新都区", "温江区", "双流区", "郫都区", "新津区",
                "金堂县", "大邑县", "蒲江县",
                "都江堰市", "彭州市", "邛崃市", "崇州市", "简阳市"
        });

        // 🌟 绵阳市 (全 - 3区、4县、1自治县、代管1市)
        districtMap.put("绵阳市", new String[]{
                "涪城区", "游仙区", "安州区",
                "三台县", "盐亭县", "梓潼县", "平武县",
                "北川羌族自治县",
                "江油市"
        });

        // 🌟 德阳市 (全 - 2区、1县、代管3市)
        districtMap.put("德阳市", new String[]{
                "旌阳区", "罗江区",
                "中江县",
                "广汉市", "什邡市", "绵竹市"
        });

        // 四川其他城市简单补全（防止点击报错）
        districtMap.put("南充市", new String[]{"顺庆区", "高坪区", "嘉陵区", "南部县", "营山县", "仪陇县", "西充县", "阆中市"});
        districtMap.put("宜宾市", new String[]{"翠屏区", "南溪区", "叙州区", "江安县", "长宁县", "高县", "筠连县", "珙县", "兴文县", "屏山县"});
        districtMap.put("泸州市", new String[]{"江阳区", "龙马潭区", "纳溪区", "泸县", "合江县", "叙永县", "古蔺县"});

        // ================= 【重庆市区县】 =================
        // 🌟 重庆市 (全 - 26区、8县、4自治县，共38个完整区划)
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

    private void saveAddressToBackend(String finalAddress) {
        RetrofitClient.getApi().updateAddress(userId, finalAddress).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                    // 同步更新本地缓存
                    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                    editor.putString("address", finalAddress);
                    editor.apply();

                    Toast.makeText(AddressActivity.this, "地址保存成功！\n" + finalAddress, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddressActivity.this, "保存失败，请稍后再试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Toast.makeText(AddressActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
    }
}