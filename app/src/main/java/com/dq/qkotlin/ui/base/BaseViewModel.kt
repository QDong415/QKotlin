package com.dq.qkotlin.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.net.*

open class BaseViewModel: ViewModel() {
    //内部使用可变的Mutable
    protected val _loadStatus = MutableLiveData<LoadState>()

    //对外开放的是final，这是谷歌官方的写法
    open val loadStatus: LiveData<LoadState> = _loadStatus

}