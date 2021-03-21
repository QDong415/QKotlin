package com.dq.qkotlin.net

class BasePageData<T> {
    var total = 0
    var totalpage = 0
    var currentpage = 0
    var nextpage = 0

    //显示数据
    var items: MutableList<T>? = null

    //返回true == 服务器告诉我，当前不是最后一页 还有下一页； false == 当前是最后一页了
    fun hasMore(): Boolean {
        return totalpage > currentpage
    }

}