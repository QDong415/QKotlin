package com.dq.qkotlin.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dq.qkotlin.net.LoadState
import com.dq.qkotlin.net.BasePageEntity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlin.collections.HashMap

/**
 * 如果你的列表界面用的是listview，那么Acitivty或Fragment里的 MyViewModel 继承自用这个VM，（T是列表的实体类）
 *
 * 特点：不监听list，只监听网络访问状态loadStatus，然后根据不同的loadStatus来直接用list；轻便简单容易理解
 * 为什么：因为listview没有recycleview的notifyItemRangeInserted,只有notity,所以干脆直接在这里就全部addAll处理好，然后界面直接notity
 */
open class BaseLVPagerViewModel<T>: BaseViewModel() {

    //下拉刷新的错误信息，服务器给我返回的 也可以自定义
    var errorMessage:String? = null

    //最核心的数据列表，我的做法是：不监听他，直接get他
    //当然也有人的做法是 LiveData<MutableList<T>> 监听onChange，然后无脑notityDataChanged，个人觉得这样做反而限制很多
    var list: MutableList<T>? = null

    //下次请求需要带上的页码参数
    private var page = 1

    open fun requireList(params : HashMap<String,String>, loadmore : Boolean , block:suspend() -> BasePageEntity<T>){

        _loadStatus.value = (if (loadmore) LoadState.PageLoading else LoadState.Loading)

        //如果是加载更多，就加上参数page；否则（下拉刷新）就强制设为1，如果服务器要求是0，就改成"0"
        params["page"] = if (loadmore) page.toString() else "1"

        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            //访问网络异常的回调用, 这种方法可以省去try catch, 但不适用于async启动的协程
            //这里是主线程；
            errorMessage = "Emm..服务器出小差了";
            _loadStatus.setValue(
                    if (loadmore) LoadState.PageNetworkFail else LoadState.NetworkFail
            )
        }

        /*viewModelScope是一个绑定到当前viewModel的作用域  当ViewModel被清除时会自动取消该作用域，所以不用担心内存泄漏为问题*/
        viewModelScope.launch(coroutineExceptionHandler) {

            //用高阶函数，直接让具体的子
            val response: BasePageEntity<T> = block();
            //如果网络访问异常，代码会直接进入CoroutineExceptionHandler，不会走这里了

            //触发activity的onChanged，让activity处理界面
            if (loadmore) {
                //加载更多
                if (response.isSuccess) {//加载更多服务器返回成功
                    page++
                    response.data?.items?.let { list?.addAll(it) }
                    _loadStatus.setValue(
                        if (response.data!!.hasMore()) LoadState.PageSuccessHasMore else LoadState.PageSuccessNoMore
                    )
                } else {
                    _loadStatus.setValue(LoadState.PageCodeError)
                }
            } else { //下拉刷新的
                if (response.isSuccess) {
                    page = 2 //页面强制设置为下次请求第2页

                    //初始化 or 重置list数据
                    if (list == null)
                        list = response.data?.items?.toMutableList()
                    else {
                        list?.clear()
                        response.data?.items?.let { list?.addAll(it) }
                    }

                    _loadStatus.setValue(
                        if (response.data!!.hasMore()) LoadState.SuccessHasMore else LoadState.SuccessNoMore
                    )
                } else {
                    _loadStatus.setValue(LoadState.CodeError)
                    errorMessage = response.message
                }
            }
        }
    }
}