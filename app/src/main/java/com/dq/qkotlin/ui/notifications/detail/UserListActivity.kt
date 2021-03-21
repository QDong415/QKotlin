package com.dq.qkotlin.ui.notifications.detail

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.dq.qkotlin.R
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.databinding.ActivityRecycleviewBinding
import com.dq.qkotlin.net.BasePageEntity
import com.dq.qkotlin.net.RetrofitInstance
import com.dq.qkotlin.net.UserApiService
import com.dq.qkotlin.tool.QApplication
import com.dq.qkotlin.ui.base.BaseRVActivity
import com.dq.qkotlin.ui.base.BaseRVPagerViewModel
import com.scwang.smart.refresh.layout.SmartRefreshLayout

/**
 * RecycleView的Demo，具体每一条的bean是UserBaseBean，VM是UserArrayViewModel
 */
class UserListActivity : BaseRVActivity<UserBaseBean, UserListActivity.UserArrayViewModel>() {

    private lateinit var adapter: UserQuickAdapter

    private lateinit var binding: ActivityRecycleviewBinding

    override fun initView() {
        super.initView()

        adapter = UserQuickAdapter(viewModel.list)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recycleview)
        initView();

        binding.refreshLayout.setOnRefreshListener {
            val params = HashMap<String, String>()
            params["keyword"] = "小"
            viewModel.requireUserList(params, false)
        }

        binding.refreshLayout.setOnLoadMoreListener {
            val params = HashMap<String, String>()
            params["keyword"] = "小"
            viewModel.requireUserList(params, true)
        }


        //demo 添加的 Header
        //Header 是自行添加进去的 View，所以 Adapter 不管理 Header 的 DataBinding。
        //请在外部自行完成数据的绑定
//        val view: View = layoutInflater.inflate(R.layout.listitem_follower, null, false)
//        view.findViewById(R.id.iv).setVisibility(View.GONE)
//        adapter.addHeaderView(view)

        binding.refreshLayout.autoRefresh(100,200,1f,false);//延迟100毫秒后自动刷新

        //item 点击事件
//        adapter.setOnItemClickListener(object : OnItemClickListener() {
//            fun onItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
//            }
//        })
    }

    override fun getTootBarTitle(): String {
        return "RecycleView列表"
    }

    //本界面对应的VM类，如果VM复杂的话，也可以独立成一个外部文件
    class UserArrayViewModel: BaseRVPagerViewModel<UserBaseBean>() {

        //按MVVM设计原则，请求网络应该放到更下一层的"仓库类"里，但是我感觉如果你只做网络不做本地取数据，没必要
        //请求用户列表接口
        fun requireUserList(params : HashMap<String,String> , loadmore : Boolean){

            //调用"万能列表接口封装"
            super.requireList(params, loadmore){

                //用kotlin高阶函数，传入本Activity的"请求用户列表接口的代码块" 就是这3行代码
                var apiService : UserApiService = RetrofitInstance.instance.create(UserApiService::class.java)
                val response: BasePageEntity<UserBaseBean> = apiService.userList(params)
                response
            }
        }
    }

    override fun adapter(): UserQuickAdapter = adapter

    override fun refreshLayout(): SmartRefreshLayout = binding.refreshLayout

    override fun onBindViewModel(): Class<UserArrayViewModel> = UserArrayViewModel::class.java
}