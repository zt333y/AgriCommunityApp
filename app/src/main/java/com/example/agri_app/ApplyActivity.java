package com.example.agri_app;

public class ApplyActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        findViewById(R.id.btn_submit_apply).setOnClickListener(v -> {
            Apply a = new Apply();
            a.setUserId(getSharedPreferences("UserPrefs", MODE_PRIVATE).getLong("userId", 0));
            a.setApplyRole(findViewById(R.id.rb_farmer).isSelected() ? 1 : 2);
            a.setRealName(((EditText)findViewById(R.id.et_real_name)).getText().toString());
            a.setIdCard(((EditText)findViewById(R.id.et_id_card)).getText().toString());
            a.setAddress(((EditText)findViewById(R.id.et_apply_address)).getText().toString());

            RetrofitClient.getApi().submitApply(a).enqueue(new Callback<Result<String>>() {
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    if (response.body() != null) {
                        Toast.makeText(ApplyActivity.this, response.body().getData(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                public void onFailure(Call<Result<String>> call, Throwable t) {}
            });
        });
    }
}