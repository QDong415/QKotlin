package com.dq.qkotlin.net

class BasePageEntity<T> {
    //  判断标示
    var code = 0

    //    提示信息
    var message: String? = null

    //显示数据（用户需要关心的数据）
    var data: BasePageData<T>? = null

    val isSuccess: Boolean
        get() = code == 1
}