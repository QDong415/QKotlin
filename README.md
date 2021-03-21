# QKotlin

Kotlin+MVVM框架，最符合实际接口情况、最接地气的封装

大家都已经看过很多MVVM的开发框架了，各式各样的都有，高star的几个项目我也基本都消化一遍，但是都感觉差了点什么。
要么封装的太过复杂，别人很难上手，实际也用不上那么复杂的封装；
要么就是为了封装而封装，实际情况很难变通；
要么就是光顾着搭建架子，实际的restful api接口根本对接不上

1、本框架主要技术关键词：
协程suspend、retrofit、smart下拉刷新、BaseRecyclerViewAdapterHelper、ViewBinding、ViewModel

2、本框架优点：
非常贴合实际项目需求，用的个别第3方的也都是最前沿的技术。不用sleep，wait模拟服务器接口，本框架直接拿实际网络接口演示

本框架针对下拉刷新、底部加载更多、判断是否有更多页、判断空布局、内存重启时候Fragment处理、、等等问题重点封装，其他无所谓的东西能不封装的就不封装，更方便接入你的项目

ViewModel里监听的接口返回情况，封装的明明白白：

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