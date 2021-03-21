package com.dq.qkotlin.ui.base

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dq.qkotlin.R
import com.dq.qkotlin.net.LoadState
import com.dq.qkotlin.tool.QApplication
import com.scwang.smart.refresh.layout.SmartRefreshLayout

/**
 * 场景：如果Activity里有RecycleView，那么就继承BaseRVActivity，T是列表数据的每条的Bean，VM 是BaseRVPagerViewModel子类
 */
open abstract class BaseRVActivity<T ,VM : BaseRVPagerViewModel<T>> : BaseAppCompatActivity() {

    protected val viewModel: VM by lazy { ViewModelProvider(this).get(onBindViewModel()) }

    override fun initView() {
        super.initView()
        initRVObservable()
    }

    //子类自己写获取adapter的方法（比如new ） 然后通过这个方法返回就行了
    //out 就是java里的<? extends BaseViewHolder> 就是可以兼容BaseViewHolder的子类
    abstract fun adapter(): BaseQuickAdapter<T, out BaseViewHolder>

    //子类自己写获取refreshLayout的方法（比如findViewById或者binding.） 然后通过这个方法返回就行了
    abstract fun refreshLayout(): SmartRefreshLayout

    //子类重写
    abstract fun onBindViewModel(): Class<VM>

    protected open fun initRVObservable() {
        //监听网络返回值
        viewModel.loadStatus
                .observe(this, Observer<Any> { loadState ->
                    when (loadState) {
                        LoadState.None -> {
                        }
                        LoadState.Loading -> {
                        }
                        LoadState.SuccessNoMore, LoadState.SuccessHasMore -> {
                            refreshLayout().finishRefresh(0)

                            adapter().setList(viewModel.tempRefreshlist!!)

                            if (loadState === LoadState.SuccessHasMore)
                                refreshLayout().finishLoadMore()
                            else refreshLayout().finishLoadMoreWithNoMoreData()

                            if (viewModel.list.isNullOrEmpty()) {
                                emptyLayout.findViewById<TextView>(R.id.empty_tv).setText("空空如也~")
                                adapter().setEmptyView(emptyLayout)
                            }
                        }
                        LoadState.CodeError, LoadState.NetworkFail -> {
                            refreshLayout().finishRefresh(0)
                            refreshLayout().finishLoadMoreWithNoMoreData()

                            if (viewModel.list.isNullOrEmpty()) {
                                emptyLayout.findViewById<TextView>(R.id.empty_tv).setText(viewModel.errorMessage)
                                adapter().setEmptyView(emptyLayout)
                            }
                        }
                        LoadState.PageLoading -> {
                        }
                        LoadState.PageSuccessHasMore , LoadState.PageSuccessNoMore-> {
                            adapter().addData(viewModel.tempPagelist!!)

                            if (loadState === LoadState.PageSuccessHasMore)
                                refreshLayout().finishLoadMore()
                            else refreshLayout().finishLoadMoreWithNoMoreData()
                        }
                        LoadState.PageCodeError, LoadState.PageNetworkFail ->
                            refreshLayout().finishLoadMoreWithNoMoreData()
                    }
                })
    }

    //空布局
    private val emptyLayout: View by lazy {
        LayoutInflater.from(this).inflate(R.layout.listview_empty, null)
    }

}