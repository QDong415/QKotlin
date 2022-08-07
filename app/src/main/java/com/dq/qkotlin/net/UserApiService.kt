package com.dq.qkotlin.net

import com.dq.qkotlin.bean.ResponseEntity
import com.dq.qkotlin.bean.ResponsePageEntity
import com.dq.qkotlin.bean.UserBaseBean
import retrofit2.http.*

interface UserApiService {

    //获取用户列表
    @GET("user/getlist")
    suspend fun userList(@QueryMap map: HashMap<String, String>) : ResponsePageEntity<UserBaseBean>

    //关注、点赞、领取
    @FormUrlEncoded
    @POST("follow/follow")
    suspend fun userFollow(@FieldMap map: HashMap<String, String>) : ResponseEntity<Object>

    //删除
    @FormUrlEncoded
    @POST("user/block")
    suspend fun userDelete(@FieldMap map: HashMap<String, String>) : ResponseEntity<Object>

    //获取详情
    @GET("user/profile")
    suspend fun userProfile(@QueryMap map: HashMap<String, String>) : ResponseEntity<UserBaseBean>

}