package com.dq.qkotlin.ui.notifications
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.net.BasePageEntity
import com.dq.qkotlin.net.RetrofitInstance
import com.dq.qkotlin.net.UserApiService
import com.dq.qkotlin.ui.base.BaseLVPagerViewModel

class NotificationsViewModel: BaseLVPagerViewModel<UserBaseBean>() {

    fun requireUserList(params : HashMap<String,String> , loadmore : Boolean){

        super.requireList(params, loadmore){
            var apiService : UserApiService = RetrofitInstance.instance.create(UserApiService::class.java)
            val response: BasePageEntity<UserBaseBean> = apiService.userList(params)
            response
        };
    }
}