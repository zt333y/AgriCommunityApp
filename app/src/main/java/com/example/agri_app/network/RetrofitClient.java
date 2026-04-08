package com.example.agri_app.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 请确保 IP 地址与你后端运行的电脑一致
    private static final String BASE_URL = "http://192.168.31.60:8080/";
    private static Retrofit retrofit = null;
    private static String authToken = ""; // 🌟 新增：静态存储 Token

    // 🌟 新增：登录成功后调用此方法，更新客户端的 Token
    public static void setToken(String token) {
        authToken = token;
        retrofit = null; // 重置单例，强制下次请求时重新创建带 Token 的客户端
    }

    public static ApiService getApi() {
        if (retrofit == null) {
            // 🌟 核心：创建一个 OkHttp 拦截器，自动在所有请求头里塞入 Token
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder();

                        // 只要 Token 不为空，就按照后端要求的格式塞入 Authorization 头
                        if (authToken != null && !authToken.isEmpty()) {
                            requestBuilder.addHeader("Authorization", "Bearer " + authToken);
                        }

                        return chain.proceed(requestBuilder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // 绑定这个“会自动刷卡”的客户端
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}