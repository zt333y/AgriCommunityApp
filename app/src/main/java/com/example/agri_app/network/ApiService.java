package com.example.agri_app.network;

import com.example.agri_app.entity.LoginResponse;
import com.example.agri_app.entity.Product;
import com.example.agri_app.entity.Result;
import com.example.agri_app.entity.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("/user/login")
    Call<Result<LoginResponse>> login(@Body User user);

    @GET("/api/product/list")
    Call<Result<java.util.List<Product>>> getProductList(@Query("keyword") String keyword);

    // 加入购物车接口
    @POST("/api/cart/add")
    Call<Result<String>> addCart(@Body com.example.agri_app.entity.Cart cart);

    // 查询购物车列表
    @GET("/api/cart/list")
    Call<Result<java.util.List<com.example.agri_app.entity.CartVO>>> getCartList(@Query("userId") Long userId);

    // 结账生成订单
    @POST("/api/order/create")
    Call<Result<String>> createOrder(@Query("userId") Long userId);

    // 查询订单列表
    @GET("/api/order/list")
    Call<Result<java.util.List<com.example.agri_app.entity.OrderVO>>> getOrderList(@Query("userId") Long userId);

    // 用户注册接口
    @POST("/user/register")
    Call<Result<String>> register(@Body User user);

    @POST("/api/admin/product/add") // 注意路径要和你后端 Controller 对应
    Call<Result<String>> addProduct(@Body Product product); // 🌟 这里的 User 改成 Product
}