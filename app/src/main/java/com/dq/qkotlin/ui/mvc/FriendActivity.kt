package com.dq.qkotlin.ui.mvc

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.dq.qkotlin.R
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.databinding.ActivityRecyclerviewBinding
import com.dq.qkotlin.net.*
import com.dq.qkotlin.tool.NET_ERROR
import com.dq.qkotlin.tool.QApplication
import com.dq.qkotlin.ui.base.INavBar
import com.dq.qkotlin.view.SpacesItemDecoration
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.*

/**
 * 全部基于最传统的MVC，只涉及refresh这一个第3方
 */
class FriendActivity : AppCompatActivity(), INavBar {

    private val title: String by lazy { intent.getStringExtra("title")!! }

    //View
    private lateinit var mAdapter: FriendRecyclerAdapter

    private lateinit var binding: ActivityRecyclerviewBinding

    //Data
    open val list: MutableList<UserBaseBean> = arrayListOf()
    //下次请求需要带上的页码参数
    private var page = 1

    //协程
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recyclerview)

        //设置状态栏
        initStatusBar(this)
        //设置toolbar
        initToolbarView(findViewById(R.id.view_stub_toolbar))

        initView()

        initListener()
    }

    private fun initView() {

        //设置RecyclerView
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recyclerView.layoutManager = layoutManager

        // 选择2：设置颜色、高度、间距等
        val itemDecoration = SpacesItemDecoration(this, SpacesItemDecoration.VERTICAL)
            .setParam(R.color.divider_color, 1, 30f, 0f)
        binding.recyclerView.addItemDecoration(itemDecoration)

        //RecyclerView创建适配器，并且设置
        mAdapter = FriendRecyclerAdapter(this, list)
        binding.recyclerView.adapter = mAdapter
    }

    private fun initListener() {
        //下拉刷新底部加载控件
        val refreshLayout: SmartRefreshLayout = findViewById(R.id.refresh_layout);

        refreshLayout.setOnRefreshListener {
            //触发了下拉刷新
            val params = HashMap<String, String>()
            params["keyword"] = "小"
            params["page"] = "1"

            requestFriendList(params, object :
                NetworkResponseCallback<ResponsePageEntity<UserBaseBean>> {
                //网络请求返回
                override fun onResponse(responseEntry: ResponsePageEntity<UserBaseBean>?, errorMessage: String?) {
                    //处理
                    handleListResponse(it, true , responseEntry, errorMessage)
                }
            })
        }

        refreshLayout.setOnLoadMoreListener {
            //触发了底部加载更多
            val params = HashMap<String, String>()
            params["keyword"] = "小"
            params["page"] = page.toString()

            requestFriendList(params, object :
                NetworkResponseCallback<ResponsePageEntity<UserBaseBean>> {
                //网络请求返回
                override fun onResponse(responseEntry: ResponsePageEntity<UserBaseBean>?, errorMessage: String?) {
                    handleListResponse(it, false , responseEntry, errorMessage)
                }
            })
        }

        //先设置为没更多了
//        refreshLayout.finishLoadMoreWithNoMoreData()

        //立即开始刷新
        refreshLayout.autoRefresh(100,200,1f,false);//延迟100毫秒后自动刷新
    }

    //请求列表，这个方法进行了初步的解耦
    private fun requestFriendList(params : HashMap<String,String>, responseCallback: NetworkResponseCallback<ResponsePageEntity<UserBaseBean>>){

        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            //访问网络异常的回调用, 这种方法可以省去try catch, 但不适用于async启动的协程
            //这里是主线程；
            Log.e("dz",throwable.toString())
            responseCallback?.onResponse(null, NET_ERROR)
        }

        /*MainScope()是一个绑定到当前viewModel的作用域  当ViewModel被清除时会自动取消该作用域，所以不用担心内存泄漏为问题*/
        scope.launch(coroutineExceptionHandler) {

            var apiService : UserApiService = RetrofitInstance.instance.create(UserApiService::class.java)
            //suspend 是这一步
            val response: ResponsePageEntity<UserBaseBean> = apiService.userList(params)
            //如果网络访问异常，代码会直接进入CoroutineExceptionHandler，不会走这里了
            //这里是主线程
            responseCallback?.onResponse(response, null)
        }
    }

    private fun handleListResponse(refreshLayout: RefreshLayout, isRefresh: Boolean = false, responseEntry: ResponsePageEntity<UserBaseBean>?, errorMessage: String?){

        if (isRefresh){
            //取消下拉刷新状态
            refreshLayout.finishRefresh(0)
        }

        if (responseEntry == null){
            //进入这里，说明是服务器崩溃，errorMessage是我们本地自定义的
            Toast.makeText(QApplication.instance, errorMessage , Toast.LENGTH_SHORT).show()
            return
        } else if (!responseEntry.isSuccess){
            //进入这里，说明是服务器验证参数错误，message是服务器返回的
            Toast.makeText(QApplication.instance, responseEntry.message , Toast.LENGTH_SHORT).show()
            return
        }

        //检查底部是否还有更多数据
        if (responseEntry.data!!.hasMore()) {
            //还有更多数据
            refreshLayout.finishLoadMore()
        } else {
            //没有更多数据
            refreshLayout.finishLoadMoreWithNoMoreData()
        }

        if (isRefresh) {
            //是下拉刷新的返回
            page = 2 //页面强制设置为下次请求第2页
            responseEntry.data?.let {
                //处理列表数据
                list.clear()
                list?.addAll(it.items!!)

                //检查显示\隐藏空布局。不同RV库对EmptyView有不同的实现，这是基于原生Adapter的方式。这一句需要写在notifyDataSetChanged前
                mAdapter.showEmptyViewEnable = true
                mAdapter.notifyDataSetChanged()
            }
        } else {
            //是底部加载更多
            page++

            responseEntry.data?.let {
                list?.addAll(it.items!!)
                //这里也可以改成：notifyItemRangeInserted
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    //FM.onPause -> AC.onPause -> FM.onStop -> AC.onStop -> FM.onDestroyView -> FM.onDestroy -> FM.onDetach -> AC.onDestroy
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel(null)
    }

    // Implements - INavBar
    override fun getTootBarTitle(): String {
        return "MVC+普通RecyclerView"
    }

    override fun onNavigationOnClick(view: View) {
        finish()
    }
}