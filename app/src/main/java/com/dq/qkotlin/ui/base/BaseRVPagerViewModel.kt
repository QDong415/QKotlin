package com.dq.qkotlin.ui.base

import androidx.lifecycle.viewModelScope
import com.dq.qkotlin.net.LoadState
import com.dq.qkotlin.net.ResponsePageEntity
import com.dq.qkotlin.tool.NET_ERROR
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlin.collections.HashMap

/**
 * 场景：如果你的列表界面用的是RecyclerView，那么Activity或Fragment里的 MyViewModel 继承这个VM，（T是列表的实体类）
 *
 * 特点：不监听list，只监听网络访问状态loadStatus，然后根据不同的loadStatus来直接用list；轻便简单容易理解
 * 为什么还有tempList这东西？：因为recyclerview有notifyItemRangeInserted,所以翻页的时候要用到这一页的templist，然后用templist做局部刷新
 */
open class BaseRVPagerViewModel<T>: BaseViewModel() {

    //下拉刷新的错误信息是服务器给我返回的，如果网络请求失败，我就本地自定义
    var errorMessage:String? = null

    //最核心的数据列表，我的做法是：不监听他，直接get他
    //当然也有人的做法是 LiveData<MutableList<T>> 然后onChange里无脑notityDataChanged，个人觉得那样做反而限制很多
    //特别注明：如果使用的是BaseRecyclerViewAdapterHelper，他的adapter里有会有个list的指针，我们这里也有个指针，但是内存共用一个
    open val list: MutableList<T> = arrayListOf()

    //下拉刷新请求返回的临时templist：
    var tempRefreshlist: List<T>? = null

    //翻页请求返回的临时templist：
    //为什么分别定义两个temp：因为极端情况下，下拉刷新和底部翻页同时请求网络，只用一个temp的话就不知道应该setList还是addList
    //注意：这样做分成两个也不会造成占用内存增加，因为我addList(tempList)之后, 立即templist = null
    var tempPagelist: List<T>? = null

    //下次请求需要带上的页码参数
    private var page = 1

    /**
     * 功能：万能的列表请求接口
     * @params get请求参数，无需page字段
     * @loadmore true = 是底部翻页，false = 下拉刷新
     * @block 具体的那两行suspend协程请求网络的代码块，其返回值是网络接口返回值
     */
    open fun requestList(params : HashMap<String,String>, loadmore : Boolean , block:suspend() -> ResponsePageEntity<T>){

        _loadStatus.value = (if (loadmore) LoadState.PageLoading else LoadState.Loading)

        //如果是加载更多，就加上参数page；否则（下拉刷新）就强制设为1，如果服务器要求是0，就改成"0"
        params["page"] = if (loadmore) page.toString() else "1"

        //访问网络异常的回调用, 这种方法可以省去try catch, 但不适用于async启动的协程
        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            //这里是主线程；
            errorMessage = NET_ERROR
            _loadStatus.setValue(
                    if (loadmore) LoadState.PageNetworkFail else LoadState.NetworkFail
            )
        }

        /*viewModelScope是一个绑定到当前viewModel的作用域  当ViewModel被清除时会自动取消该作用域，所以不用担心内存泄漏为问题*/
        viewModelScope.launch(coroutineExceptionHandler) {

            //具体的那两行suspend协程请求网络的代码 由VM子类来实现
            val response: ResponsePageEntity<T> = block();
            //如果网络访问异常，代码会直接进入CoroutineExceptionHandler，不会走这里了

            if (loadmore) {
                //加载更多
                if (response.isSuccess) {//加载更多服务器返回成功
                    page++

                    //这次底部翻页接口返回的具体List<Bean>
                    tempPagelist = response.data?.items

                    //触发activity的onChanged，让activity处理界面
                    _loadStatus.setValue(
                        if (response.data!!.hasMore()) LoadState.PageSuccessHasMore else LoadState.PageSuccessNoMore
                    )

                    //代码走到这里，tempPagelist已经用完了（把他addAll了），就立即释放掉temp的内存
                    tempPagelist = null;

                } else {
                    _loadStatus.setValue(LoadState.PageCodeError)
                }
            } else { //下拉刷新请求完毕
                if (response.isSuccess) {
                    page = 2 //页面强制设置为下次请求第2页

                    //这次下拉刷新接口返回的具体List<Bean>
                    tempRefreshlist = response.data?.items

                    //触发activity的onChanged，让activity处理界面
                    _loadStatus.setValue(
                        if (response.data!!.hasMore()) LoadState.SuccessHasMore else LoadState.SuccessNoMore
                    )

                    //代码走到这里，界面已经用过了tempRefreshlist（把他addAll了），就立即释放掉temp的内存
                    tempRefreshlist = null;

                } else {
                    //服务器告诉我参数错误
                    _loadStatus.setValue(LoadState.CodeError)
                    errorMessage = response.message
                }
            }
        }
    }
}