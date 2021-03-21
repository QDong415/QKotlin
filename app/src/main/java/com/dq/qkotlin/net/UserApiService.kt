package com.dq.qkotlin.net

import com.dq.qkotlin.bean.UserBaseBean
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface UserApiService {

    //获取用户列表
    @GET("user/getlist")
    suspend fun userList(@QueryMap map: HashMap<String, String>) : BasePageEntity<UserBaseBean>
}