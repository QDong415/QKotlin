package com.dq.qkotlin.net

//具体的网络接口返回情况
enum class LoadState {
    None,
    Loading, //下拉刷新开始请求接口 or 普通开始请求接口
    SuccessHasMore, //下拉刷新请求成功且服务器告诉我还有下一页 or 普通请求成功
    SuccessNoMore,  //下拉刷新请求成功且服务器告诉我已经没有下一页了
    CodeError, //下拉刷新请求成功但是服务器给我返回了错误的code码 or 普通请求成功但是服务器给我返回了错误的code码
    NetworkFail, //下拉刷新请求失败 or 普通请求失败，原因是压根就没访问到服务器
    PageLoading,  //底部翻页开始请求接口
    PageSuccessHasMore, //底部翻页请求成功且服务器告诉我还有下一页
    PageSuccessNoMore, //底部翻页请求成功且服务器告诉我已经没有下一页了
    PageCodeError, //底部翻页请求成功但是服务器给我返回了错误的code码
    PageNetworkFail, //底部翻页请求失败，原因是压根就没访问到服务器
}