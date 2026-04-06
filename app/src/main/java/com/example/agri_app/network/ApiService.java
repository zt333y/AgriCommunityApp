package com.example.agri_app.network;
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
    @POST("/api/user/login")
    Call<Result<User>> login(@Body User user);

    @GET("/api/product/list")
    Call<Result<java.util.List<com.example.agri_app.entity.Product>>> getProductList(@retrofit2.http.Query("keyword") String keyword);

    // 加入购物车接口
    @POST("/api/cart/add")
    Call<Result<String>> addCart(@Body com.example.agri_app.entity.Cart cart);

    // 查询购物车列表
    @GET("/api/cart/list")
    Call<Result<java.util.List<com.example.agri_app.entity.CartVO>>> getCartList(@Query("userId") Long userId);

    // 结账生成订单
    @POST("/api/order/create")
    Call<Result<String>> createOrder(@retrofit2.http.Query("userId") Long userId);

    // 查询订单列表
    @GET("/api/order/list")
    Call<Result<java.util.List<com.example.agri_app.entity.OrderVO>>> getOrderList(@retrofit2.http.Query("userId") Long userId);
}