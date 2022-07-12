package com.dq.qkotlin.ui.notifications

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.net.*
import com.dq.qkotlin.tool.requestCommon

class ProfileViewModel : ViewModel() {

    //监听整个Model
    private val _profileLiveData = MutableLiveData<UserBaseBean>()
    val profileLiveData = _profileLiveData

    //监听整个Model请求状态
    private val _loadingProfileLiveData = MutableLiveData<Boolean>()
    val loadingProfileLiveData = _loadingProfileLiveData

    //监听整个Model请求错误message
    private val _toastLiveData = MutableLiveData<String>()
    val toastLiveData = _toastLiveData

    fun requestUserProfile(params: HashMap<String, String>) {

        _loadingProfileLiveData.value = true

        requestCommon(viewModelScope, {

            var apiService : UserApiService = RetrofitInstance.instance.create(UserApiService::class.java)
            //suspend 是这一步
            val responseEntry: ResponseEntity<UserBaseBean> = apiService.userProfile(params)

            //给Activity回调加载完成
            _loadingProfileLiveData.value = false

            if (responseEntry.isSuccess){
                //给Activity回调成功
                _profileLiveData.value = responseEntry.data
            } else {
                //进入这里，说明是服务器验证参数错误，message是服务器返回的
                _toastLiveData.value = responseEntry.message
            }

        }, object : NetworkFailCallback {

            //说明是404、500
            override fun onResponseFail(code: Int, errorMessage: String?) {
                //这里是主线程；
                _loadingProfileLiveData.value = false
                _toastLiveData.value = errorMessage
            }
        })
    }
}