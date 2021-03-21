package com.dq.qkotlin.bean

import java.io.Serializable

class UserBaseBean : Serializable {
    var userid: String? = null
    var name: String? = null
    var avatar: String? = null
    var gender = 0
    var age = 0
    var create_time = 0
    var follow = 0

}