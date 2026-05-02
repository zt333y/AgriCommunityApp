package com.example.agri_app.network;

import com.example.agri_app.entity.Apply;
import com.example.agri_app.entity.LoginResponse;
import com.example.agri_app.entity.Notice;
import com.example.agri_app.entity.OrderVO;
import com.example.agri_app.entity.Product;
import com.example.agri_app.entity.Result;
import com.example.agri_app.entity.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface ApiService {

    @POST("/user/login")
    Call<Result<LoginResponse>> login(@Body User user);

    // 加入购物车接口
    @POST("/api/cart/add")
    Call<Result<String>> addCart(@Body com.example.agri_app.entity.Cart cart);

    // 查询购物车列表
    @GET("/api/cart/list")
    Call<Result<java.util.List<com.example.agri_app.entity.CartVO>>> getCartList(@Query("userId") Long userId);

    // 结账生成订单 (🌟 新增传入 address 参数)
    @POST("/api/order/create")
    Call<Result<String>> createOrder(@Query("userId") Long userId, @Query("address") String address);

    // 查询订单列表
    @GET("/api/order/list")
    Call<Result<java.util.List<com.example.agri_app.entity.OrderVO>>> getOrderList(@Query("userId") Long userId);

    // 用户注册接口
    @POST("/user/register")
    Call<Result<String>> register(@Body User user);

    // 🌟 新增：专门用来上传图片的接口
    @Multipart
    @POST("/api/file/upload")
    Call<Result<String>> uploadImage(@Part MultipartBody.Part file);

    @POST("/api/product/add")
    Call<Result<String>> addProduct(@Body Product product);

    // 🌟 新增：确认收货接口
    @POST("/api/order/receive")
    Call<Result<String>> receiveOrder(@Query("orderId") Long orderId);

    // 🌟 新增：农户获取自己发布的商品库
    @GET("/api/product/my")
    Call<Result<java.util.List<Product>>> getMyProducts();

    // 🌟 新增：删除/下架商品接口
    @POST("/api/product/delete")
    Call<Result<String>> deleteProduct(@Query("id") Long id);

    // 🌟 新增：提交评价接口
    @retrofit2.http.POST("/api/review/add")
    Call<Result<String>> addReview(@retrofit2.http.Body com.example.agri_app.entity.Review review);

    // 🌟 团长获取订单列表
    @GET("/api/order/leaderList")
    Call<Result<java.util.List<OrderVO>>> getLeaderOrders();

    // 🌟 团长确认到货 (入库)
    @POST("/api/order/arrive")
    Call<Result<String>> arriveOrder(@Query("orderId") Long orderId);

    // 🌟 团长核销订单 (出库)
    @POST("/api/order/verify")
    Call<Result<String>> verifyOrder(@Query("orderId") Long orderId);

    // 用户提交资质入驻申请
    @POST("/api/apply/submit")
    Call<Result<String>> submitApply(@Body Apply apply);

    // 🌟 新增：农户获取今日采摘/发货汇总清单
    @GET("/api/order/pickingList")
    Call<Result<java.util.List<com.example.agri_app.entity.FarmerPickingVO>>> getPickingList();

    // 🌟 农户专属：根据商品一键发货
    @POST("/api/order/shipByProduct")
    Call<Result<String>> shipByProduct(@Query("productId") Long productId);

    // 🌟 新增：更新用户收货地址
    @POST("/user/updateAddress")
    Call<Result<String>> updateAddress(@Query("userId") Long userId, @Query("address") String address);

    // 🌟 新增：修改商品的请求
    @POST("/api/product/update")
    Call<Result<String>> updateProduct(@Body Product product);

    // 🌟 新增：获取商品评价列表
    @GET("/api/review/product")
    Call<Result<java.util.List<com.example.agri_app.entity.ReviewVO>>> getProductReviews(@Query("productId") Long productId);

    // 🌟 新增：获取订单中的所有商品，用于分别评价
    @GET("/api/order/items")
    Call<Result<java.util.List<com.example.agri_app.entity.OrderItem>>> getOrderItems(@Query("orderId") Long orderId);

    // 🌟 修改个人资料（传对象过去，包含 id, nickname, password 等字段）
    @POST("/user/update")
    Call<com.example.agri_app.entity.Result<String>> updateProfile(@Body com.example.agri_app.entity.User user);

    // 1. 获取商品列表（支持模糊搜索）
    @GET("/api/product/list")
    Call<Result<List<Product>>> getProductList(@Query("keyword") String keyword);

    // 2. 获取首页滚动公告列表
    @GET("/api/notice/list")
    Call<Result<List<Notice>>> getNoticeList();

    // 🌟 在里面加上修改数量和删除的接口
    @POST("/api/cart/updateQuantity")
    Call<Result<String>> updateCartQuantity(@Query("cartId") Long cartId, @Query("quantity") Integer quantity);

    @POST("/api/cart/delete")
    Call<Result<String>> deleteCartItem(@Query("cartId") Long cartId);

    // 🌟 新增：商品上下架接口
    @POST("/api/product/updateStatus")
    Call<Result<String>> updateProductStatus(@Query("id") Long id, @Query("status") Integer status);

    // 🌟 新增：用户申请售后接口
    @POST("/order/applyAfterSales") // 请确保这里的路径与你后端 Controller 保持一致
    Call<Result<String>> applyAfterSales(@Query("orderId") Long orderId, @Query("reason") String reason);

    // 🌟 新增：团长确认收到用户退货
    @POST("/order/leaderConfirmReturn")
    Call<Result<String>> leaderConfirmReturn(@Query("orderId") Long orderId);
}