package com.dq.qkotlin.ui.home.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.dq.qkotlin.R
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.databinding.ActivityRecyclerviewBinding
import com.dq.qkotlin.net.*
import com.dq.qkotlin.tool.QApplication
import com.dq.qkotlin.ui.base.BaseRVActivity
import com.dq.qkotlin.ui.base.INavBar
import com.dq.qkotlin.view.SpacesItemDecoration
import com.kongzue.dialogx.dialogs.WaitDialog
import com.scwang.smart.refresh.layout.SmartRefreshLayout

/**
 * Mvvm + RecyclerView的Demo，具体每一条的bean是UserBaseBean，VM是UserArrayViewModel
 */
class UserRVActivity : BaseRVActivity<UserBaseBean, UserRVViewModel>() , INavBar {

    //dqerror
    private var debugerror = 1

    private lateinit var adapter: UserQuickAdapter

    private lateinit var binding: ActivityRecyclerviewBinding

    private fun initView() {

        //设置状态栏
        initStatusBar(this)
        //设置toolbar
        initToolbarView(findViewById(R.id.view_stub_toolbar))

        adapter = UserQuickAdapter(viewModel.list)

        // 先注册需要点击的子控件id（注意，请不要写在convert方法里）
        adapter.addChildClickViewIds(R.id.follow_button, R.id.delete_button);

        adapter.setOnItemChildClickListener(object : OnItemChildClickListener {
            override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {

                val itemBean: UserBaseBean = viewModel.list!!.get(position)
                when(view?.id) {
                    R.id.follow_button -> {
                        //关注用户
                        if (position % 2 == 0) {
                            //MVC方式去关注
                            executeFollowUserByMVC(position, itemBean)
                        } else {
                            //MVVM方式去关注
                            lazyCreateFollowLiveData()

                            val params = HashMap<String, String>()
                            params["userid"] = "1"
                            params["to_userid"] = itemBean.userid.toString()
                            viewModel.requestFollowByMVVM(itemBean.userid , itemBean.follow, if (itemBean.follow == 1) 0 else 1 , params)
                        }
                    }
                    R.id.delete_button -> {
                        //删除用户
                        if (position % 2 == 0) {
                            //MVC方式去删除
                            executeDeleteUserByMVC(position, itemBean)
                        } else {
                            //MVVM方式去删除
                            lazyCreateDeleteLiveData()

                            val params = HashMap<String, String>()
                            params["userid"] = "1"
                            params["to_userid"] = itemBean.userid.toString()
                            viewModel.requestDeleteByMVVM(itemBean.userid ,params)
                        }
                    }
                }
            }
        })

        // 设置颜色、高度、间距等
        val itemDecoration = SpacesItemDecoration(this, SpacesItemDecoration.VERTICAL)
            .setParam(R.color.divider_color, 1, 30f, 0f)
        binding.recyclerView.addItemDecoration(itemDecoration)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recyclerview)
        initView()
        initRVObservable()

        binding.refreshLayout.setOnRefreshListener {
            val params = HashMap<String, String>()
            params["userid"] = "1"
            params["keyword"] = "大"
            params["error"] = debugerror.toString()

            viewModel.requestUserList(params, false)

            debugerror = 0
        }

        binding.refreshLayout.setOnLoadMoreListener {
            val params = HashMap<String, String>()
            params["userid"] = "1"
            params["keyword"] = "大"
            viewModel.requestUserList(params, true)
        }


        //demo 添加的 Header
        //Header 是自行添加进去的 View，所以 Adapter 不管理 Header 的 DataBinding。
        //请在外部自行完成数据的绑定
//        val view: View = layoutInflater.inflate(R.layout.listitem_follower, null, false)
//        view.findViewById(R.id.iv).setVisibility(View.GONE)
//        adapter.addHeaderView(view)

        binding.refreshLayout.autoRefresh(100,200,1f,false);//延迟100毫秒后自动刷新

        //item 点击事件
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val params = HashMap<String, String>()
                params["userid"] = "1"
                params["keyword"] = "大"
                params["error"] = "1"
                viewModel.requestListByMVC(params, object : NetworkSuccessCallback<PageData<UserBaseBean>>{

                    override fun onResponseSuccess(responseEntry: PageData<UserBaseBean>?) {
                        Log.e("dz","成功 onResponse = "+ responseEntry!!.items!!.size)
                    }

                }, object : NetworkFailCallback {

                    override fun onResponseFail(code: Int, errorMessage: String?) {
                        Log.e("dz","失败 onResponse = "+ errorMessage)
                    }
                })
            }
        })
    }


    //去关注某用户 ---- 采用MVC的写法
    private fun executeFollowUserByMVC(position: Int, itemBean: UserBaseBean){

        //逻辑：先本地设置data数据，然后再请求服务器，如果服务器访问失败，再把本地data设置回来。这样用户体验更好
        itemBean.follow = if (itemBean.follow == 1) 0 else 1
        adapter!!.notifyItemChanged(position)

        val params = HashMap<String, String>()
        params["userid"] = "1"
        params["to_userid"] = itemBean.userid.toString()
        viewModel.requestFollowByMVC(params, object :
            NetworkResponseCallback<ResponseEntity<Object>> {
            //网络请求返回
            override fun onResponse(responseEntry: ResponseEntity<Object>?, errorMessage: String?) {

                if (responseEntry == null){
                    //进入这里，说明是服务器崩溃，errorMessage是我们本地自定义的
                    Toast.makeText(QApplication.instance, errorMessage , Toast.LENGTH_SHORT).show()

                    itemBean.follow = if (itemBean.follow == 1) 0 else 1
                    adapter!!.notifyItemChanged(position)

                    return
                } else if (!responseEntry.isSuccess){
                    //进入这里，说明是服务器验证参数错误，message是服务器返回的
                    Toast.makeText(QApplication.instance, responseEntry.message , Toast.LENGTH_SHORT).show()

                    itemBean.follow = if (itemBean.follow == 1) 0 else 1
                    adapter!!.notifyItemChanged(position)

                    return
                }
            }
        })
    }

    //因为不是每次进入Activity都会调用这个接口，但是如果你每次都直接就初始化这个livedata，会额外多初始化一些系统livedata源码里的全局变量，浪费内存。所以这里做成懒加载
    private fun lazyCreateFollowLiveData(){
        if (viewModel.followRequestStateMap == null){
            viewModel.followRequestStateMap = MutableLiveData<Pair<Int ,Int>>()

            //MVVM 关注某用户，key是userid（int） value是：请求中、请求成功、请求失败。
            viewModel.followRequestStateMap!!.observe(this, Observer { loadStateMap: Pair<Int ,Int> ->

                val to_userid: Int = loadStateMap.first
                val idealState: Int = loadStateMap.second // 0 ：最新的状态是"未关注" ；1：最新的状态是"已关注"

                Log.e("dz","HomeFragment 关注收到通知"+idealState)

                for ((index, itemBean) in viewModel.list!!.withIndex()) {
                    if (itemBean.userid == to_userid){
                        itemBean.follow = idealState
                        adapter!!.notifyItemChanged(index)
                        break
                    }
                }
            })
        }
    }


    //删除Item ---- 采用MVC的写法
    private fun executeDeleteUserByMVC(position: Int, itemBean: UserBaseBean){
        WaitDialog.show("MVC写法加载中..")
        val params = HashMap<String, String>()
        params["userid"] = "1"
        params["to_userid"] = itemBean.userid.toString()


        viewModel.requestDeleteByMVC(params, object :
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

                viewModel.list!!.remove(itemBean)
                adapter!!.notifyItemRemoved(position)

            }
        })
    }

    //因为不是每次进入Activity都会调用这个接口，但是如果你每次都直接就初始化这个livedata，会额外多初始化一些系统livedata源码里的全局变量，浪费内存。所以这里做成懒加载
    private fun lazyCreateDeleteLiveData(){
        if (viewModel.deleteRequestStatePair == null){
            viewModel.deleteRequestStatePair = MutableLiveData<Pair<Int ,LoadState>>()

            //MVVM 关注某用户，key是userid（int） value是：请求中、请求成功、请求失败。
            viewModel.deleteRequestStatePair!!.observe(this, Observer { loadStateMap: Pair<Int ,LoadState> ->

                val to_userid: Int = loadStateMap.first
                val loadState: LoadState = loadStateMap.second

                when (loadState) {
                    LoadState.Loading -> {
                        WaitDialog.show("MVVM写法加载中..")
                    }
                    LoadState.SuccessNoMore -> {
                        //删除成功
                        WaitDialog.dismiss()
                        for ((index, itemBean) in viewModel.list!!.withIndex()) {
                            if (itemBean.userid == to_userid){
                                viewModel.list!!.remove(itemBean)
                                adapter!!.notifyItemChanged(index)
                                break
                            }
                        }
                    }
                    LoadState.CodeError, LoadState.NetworkFail -> {
                        //删除失败
                        WaitDialog.dismiss()
                        Toast.makeText(QApplication.instance, viewModel.deleteRequestErrorMessage , Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    override fun adapter(): UserQuickAdapter = adapter

    override fun refreshLayout(): SmartRefreshLayout = binding.refreshLayout

    override fun onBindViewModel(): Class<UserRVViewModel> = UserRVViewModel::class.java

    // Implements - INavBar
    override fun getTootBarTitle(): String {
        return "Mvvm+BaseRecyclerViewAdapterHelper"
    }

    override fun onNavigationOnClick(view: View) {
        finish()
    }

    //toolbar用什么布局
//    override fun getBindToolbarLayout(): Int {
//        return R.layout.toolbar_draw
//    }

}