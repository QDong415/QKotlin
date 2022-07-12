package com.dq.qkotlin.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dq.qkotlin.R
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.databinding.ActivityListviewBinding
import com.dq.qkotlin.net.ResponseEntity
import com.dq.qkotlin.net.LoadState
import com.dq.qkotlin.net.NetworkResponseCallback
import com.dq.qkotlin.tool.QApplication
import com.dq.qkotlin.ui.home.detail.UserRVActivity
import com.dq.qkotlin.ui.base.INavBar
import com.kongzue.dialogx.dialogs.WaitDialog

//基于MVVM + ListView + ViewBinding
class HomeFragment : Fragment(), INavBar {

    //为了展示ErrorView 下拉刷新后变成正常列表
    private var debugError = 1;

    companion object {
        val TAG = "HomeFragment"
    }

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var binding: ActivityListviewBinding

    //本类的做法是：只有下拉刷新的时候才new adapter 并 setAdapter。你也可以修改成onCreate里就初始化，也可以尝试一下直接用binding.adapter，不用这个全局变量了
    private var adapter: HomeListViewAdapter? = null

    //空布局
    private val emptyLayout: View by lazy {
        LayoutInflater.from(activity).inflate(R.layout.listview_empty, null)
    }

    //AC.onCreate -> FM.onAttach -> FM.onCreate -> FM.onCreateView -> FM.onActivityCreated
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_listview, container,false)
        val root: View = binding.getRoot()
        return root
    }

    //... -> FM.onCreateView -> FM.onActivityCreated -> FM.onStart -> AC.onStart -> AC.onResume -> FM.onResume
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()

        homeViewModel.loadStatus
                .observe(viewLifecycleOwner, Observer<LoadState> { loadState: LoadState ->
                    Log.e("dz","HomeFragment 收到通知"+loadState)
                    when (loadState) {
                        LoadState.None -> {
                        }
                        LoadState.Loading -> {
                        }
                        LoadState.SuccessNoMore, LoadState.SuccessHasMore -> {
                            //下拉刷新成功
                            binding.refreshLayout.finishRefresh(0)

                            if (adapter == null) {
                                //说明是第一次下拉刷新成功
                                adapter = HomeListViewAdapter(requireContext() ,homeViewModel.list!! , R.layout.listitem_follower)
                                adapter!!.setOnItemClickListener(object : HomeListViewAdapter.OnItemClickListener {

                                    override fun onItemClick(view: View, position: Int) {
                                        //Item的view的点击事件
                                        val itemBean: UserBaseBean = homeViewModel.list!!.get(position)
                                        if (view.id == R.id.follow_button){
                                            //关注用户
                                            if (position % 2 == 0) {
                                                //MVC方式去关注
                                                executeFollowUserByMVC(itemBean)
                                            } else {
                                                //MVVM方式去关注
                                                lazyCreateFollowLiveData()

                                                val params = HashMap<String, String>()
                                                params["userid"] = "1"
                                                params["to_userid"] = itemBean.userid.toString()
                                                homeViewModel.requestFollowByMVVM(itemBean.userid , itemBean.follow, if (itemBean.follow == 1) 0 else 1 , params)
                                            }

                                        } else if (view.id == R.id.delete_button){
                                            //删除用户
                                            if (position % 2 == 0) {
                                                //MVC方式去删除
                                                executeDeleteUserByMVC(itemBean)
                                            } else {
                                                //MVVM方式去删除
                                                lazyCreateDeleteLiveData()

                                                val params = HashMap<String, String>()
                                                params["userid"] = "1"
                                                params["to_userid"] = itemBean.userid.toString()
                                                homeViewModel.requestDeleteByMVVM(itemBean.userid ,params)
                                            }

                                        }
                                    }
                                })
                                //这一步是利用DataBinding触发数据刷新，相当于setAdapter
                                binding.adapter = adapter
                            } else {
                                //说明是第N次下拉刷新成功
                                adapter!!.notifyDataSetChanged()
                            }

                            if (loadState === LoadState.SuccessHasMore) {
                                //下拉刷新成功 且 还有更多数据
                                binding.refreshLayout.finishLoadMore()
                            } else {
                                //下拉刷新成功 且 没有更多数据
                                binding.refreshLayout.finishLoadMoreWithNoMoreData()
                            }

                            //检查是否显示\隐藏EmptyView
                            if (homeViewModel.list!!.isEmpty()) {
                                //这个setEmptyView是我自己封装的，如果有数据了系统底层自己会帮我们隐藏emptyLayout
                                binding.listView.setEmptyView(emptyLayout)
                            }
                        }
                        LoadState.CodeError, LoadState.NetworkFail -> {
                            //下拉刷新失败
                            binding.refreshLayout.finishRefresh(0)
                            binding.refreshLayout.finishLoadMoreWithNoMoreData()

                            //设置ErrorView
                            emptyLayout.findViewById<TextView>(R.id.empty_tv).setText(homeViewModel.errorMessage)
                            binding.listView.setEmptyView(emptyLayout)
                        }
                        LoadState.PageLoading -> {
                        }
                        LoadState.PageSuccessHasMore -> {
                            //底部加载更多成功 且 还有更多数据
                            adapter?.notifyDataSetChanged()
                            binding.refreshLayout.finishLoadMore()
                        }
                        LoadState.PageSuccessNoMore -> {
                            //底部加载更多成功 且 没有更多数据了
                            adapter?.notifyDataSetChanged()
                            binding.refreshLayout.finishLoadMoreWithNoMoreData()
                        }
                        LoadState.PageCodeError, LoadState.PageNetworkFail ->
                            //底部加载失败
                            binding.refreshLayout.finishLoadMore()
                    }
                })


        binding.refreshLayout.setOnRefreshListener {
            //触发下拉刷新
            val params = HashMap<String, String>()
            params["keyword"] = "小"
            params["userid"] = "1"
            homeViewModel.requestUserList(params, false)
        }

        binding.refreshLayout.setOnLoadMoreListener {
            val params = HashMap<String, String>()
            params["keyword"] = "小"
            params["userid"] = "1"
            homeViewModel.requestUserList(params, true)
        }

        binding.listView.setOnItemClickListener{ adapterView: AdapterView<*>?, view: View?, position: Int, l: Long ->

            val i = Intent(context, UserRVActivity::class.java)
            i.putExtra("param1",homeViewModel.list!!.get(position));
            startActivity(i)
        }

        binding.refreshLayout.autoRefresh(100,200,1f,false);//延迟100毫秒后自动刷新
    }

    private fun initView(){
        initStatusBar(this)
        initToolbarView( requireView().findViewById(R.id.view_stub_toolbar))
    }

    //去关注某用户 ---- 采用MVC的写法
    private fun executeFollowUserByMVC(itemBean: UserBaseBean){

        //逻辑：先本地设置data数据，然后再请求服务器，如果服务器访问失败，再把本地data设置回来。这样用户体验更好
        itemBean.follow = if (itemBean.follow == 1) 0 else 1
        adapter!!.notifyDataSetChanged()

        val params = HashMap<String, String>()
        params["userid"] = "1"
        params["to_userid"] = itemBean.userid.toString()
        homeViewModel.requestFollowByMVC(params, object :
            NetworkResponseCallback<ResponseEntity<Object>> {
            //网络请求返回
            override fun onResponse(responseEntry: ResponseEntity<Object>?, errorMessage: String?) {

                if (responseEntry == null){
                    //进入这里，说明是服务器崩溃，errorMessage是我们本地自定义的
                    Toast.makeText(QApplication.instance, errorMessage , Toast.LENGTH_SHORT).show()

                    itemBean.follow = if (itemBean.follow == 1) 0 else 1
                    adapter!!.notifyDataSetChanged()

                    return
                } else if (!responseEntry.isSuccess){
                    //进入这里，说明是服务器验证参数错误，message是服务器返回的
                    Toast.makeText(QApplication.instance, responseEntry.message , Toast.LENGTH_SHORT).show()

                    itemBean.follow = if (itemBean.follow == 1) 0 else 1
                    adapter!!.notifyDataSetChanged()

                    return
                }
            }
        })
    }

    //因为不是每次进入Activity都会调用这个接口，但是如果你每次都直接就初始化这个livedata，会额外多初始化一些系统livedata源码里的全局变量，浪费内存。所以这里做成懒加载
    private fun lazyCreateFollowLiveData(){
        if (homeViewModel.followRequestStateMap == null){
            homeViewModel.followRequestStateMap = MutableLiveData<Pair<Int ,Int>>()

            //MVVM 关注某用户，key是userid（int） value是：请求中、请求成功、请求失败。
            homeViewModel.followRequestStateMap!!.observe(viewLifecycleOwner, Observer { loadStateMap: Pair<Int ,Int> ->

                val to_userid: Int = loadStateMap.first
                val idealState: Int = loadStateMap.second // 0 ：最新的状态是"未关注" ；1：最新的状态是"已关注"

                Log.e("dz","HomeFragment 关注收到通知"+idealState)

                for (itemBean in homeViewModel.list!!) {
                    if (itemBean.userid == to_userid){
                        itemBean.follow = idealState
                        adapter!!.notifyDataSetChanged()
                        break
                    }
                }
            })
        }
    }


    //删除Item ---- 采用MVC的写法
    private fun executeDeleteUserByMVC(itemBean: UserBaseBean){
        WaitDialog.show("MVC写法加载中..")
        val params = HashMap<String, String>()
        params["userid"] = "1"
        params["to_userid"] = itemBean.userid.toString()
        homeViewModel.requestDeleteByMVC(params, object :
            NetworkResponseCallback<ResponseEntity<Object>> {
            //网络请求返回
            override fun onResponse(responseEntry: ResponseEntity<Object>?, errorMessage: String?) {
                //关闭弹框
                WaitDialog.dismiss()

                if (responseEntry == null){
                    //进入这里，说明是服务器崩溃，errorMessage是我们本地自定义的
                    Toast.makeText(QApplication.instance, errorMessage , Toast.LENGTH_SHORT).show()
                    return
                } else if (!responseEntry.isSuccess){
                    //进入这里，说明是服务器验证参数错误，message是服务器返回的
                    Toast.makeText(QApplication.instance, responseEntry.message , Toast.LENGTH_SHORT).show()
                    return
                }

                homeViewModel.list!!.remove(itemBean)
                adapter!!.notifyDataSetChanged()

            }
        })
    }

    //因为不是每次进入Activity都会调用这个接口，但是如果你每次都直接就初始化这个livedata，会额外多初始化一些系统livedata源码里的全局变量，浪费内存。所以这里做成懒加载
    private fun lazyCreateDeleteLiveData(){
        if (homeViewModel.deleteRequestStatePair == null){
            homeViewModel.deleteRequestStatePair = MutableLiveData<Pair<Int ,LoadState>>()

            //MVVM 关注某用户，key是userid（int） value是：请求中、请求成功、请求失败。
            homeViewModel.deleteRequestStatePair!!.observe(viewLifecycleOwner, Observer { loadStateMap: Pair<Int ,LoadState> ->

                val to_userid: Int = loadStateMap.first
                val loadState: LoadState = loadStateMap.second

                when (loadState) {
                    LoadState.Loading -> {
                        WaitDialog.show("MVVM写法加载中..")
                    }
                    LoadState.SuccessNoMore -> {
                        //删除成功
                        WaitDialog.dismiss()
                        for (itemBean in homeViewModel.list!!) {
                            if (itemBean.userid == to_userid){
                                homeViewModel.list!!.remove(itemBean)
                                adapter!!.notifyDataSetChanged()
                                break
                            }
                        }
                    }
                    LoadState.CodeError, LoadState.NetworkFail -> {
                        //删除失败
                        WaitDialog.dismiss()
                        Toast.makeText(QApplication.instance, homeViewModel.deleteRequestErrorMessage , Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    override fun getTootBarTitle(): String {
        return "MVVM+ListView+viewBindng"
    }

    override fun getToolBarLeftIcon(): Int {
        return 0
    }
}