package com.dq.qkotlin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dq.qkotlin.R
import com.dq.qkotlin.databinding.ActivityListviewBinding
import com.dq.qkotlin.net.LoadState
import com.dq.qkotlin.ui.base.BaseFragment
import com.dq.qkotlin.ui.home.detail.UserListActivity
import com.yang.databindingdemo.listView.adapter.ListViewAdapter

class HomeFragment : BaseFragment() {

    companion object {
        val TAG = "HomeFragment"
    }

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var binding: ActivityListviewBinding

    private var adapter: ListViewAdapter? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        binding = DataBindingUtil.inflate(inflater, R.layout.activity_listview, container,false)
        val root: View = binding.getRoot()
        initCommonView(root)
        return root
    }

    override fun initCommonView(root: View){
        super.initCommonView(root)
        //主tab上的界面，listview会被底部的bottomNavView遮挡，如果有更好的解决办法，希望能联系我
        root.setPadding(0,0,0, resources.getDimension(R.dimen.bottom_bar_height).toInt())
    }

    override fun initData() {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        homeViewModel.loadStatus
                .observe(viewLifecycleOwner, Observer<Any> { loadState ->
                    when (loadState) {
                        LoadState.None -> {
                        }
                        LoadState.Loading -> {
                        }
                        LoadState.SuccessNoMore, LoadState.SuccessHasMore -> {
                            binding.refreshLayout.finishRefresh(0)
                            adapter = ListViewAdapter(
                                    requireContext() ,homeViewModel.list!! , R.layout.listitem_follower
                            )
                            binding.adapter = adapter
                            if (loadState === LoadState.SuccessHasMore) {
                                binding.refreshLayout.finishLoadMore()
                            } else {
                                binding.refreshLayout.finishLoadMoreWithNoMoreData()
                            }
                            if (homeViewModel.list!!.isEmpty()) {
                                binding.listView.setEmptyView(emptyLayout)
                            }
                        }
                        LoadState.CodeError, LoadState.NetworkFail -> {
                            binding.refreshLayout.finishRefresh(0)
                            binding.refreshLayout.finishLoadMoreWithNoMoreData()
                            binding.listView.setEmptyView(emptyLayout)
                        }
                        LoadState.PageLoading -> {
                        }
                        LoadState.PageSuccessHasMore -> {
                            adapter!!.notifyDataSetChanged()
                            binding.refreshLayout.finishLoadMore()
                        }
                        LoadState.PageSuccessNoMore -> {
                            adapter!!.notifyDataSetChanged()
                            binding.refreshLayout.finishLoadMoreWithNoMoreData()
                        }
                        LoadState.PageCodeError, LoadState.PageNetworkFail ->
                            binding.refreshLayout.finishLoadMoreWithNoMoreData()
                    }
                })

        binding.refreshLayout.setOnRefreshListener {
            val params = HashMap<String, String>()
            params["keyword"] = "小"
            homeViewModel.requireUserList(params, false)
        }

        binding.refreshLayout.setOnLoadMoreListener {
            val params = HashMap<String, String>()
            params["keyword"] = "小"
            homeViewModel.requireUserList(params, true)
        }

        binding.listView.setOnItemClickListener{ adapterView: AdapterView<*>?, view: View?, position: Int, l: Long ->
            startActivity<UserListActivity> {
                putExtra("param1", "data1")
                putExtra("param2", "data2")}
        }

        binding.refreshLayout.autoRefresh(100,200,1f,false);//延迟100毫秒后自动刷新
    }

    private val emptyLayout: View by lazy{
        LayoutInflater.from(activity).inflate(R.layout.listview_empty, null)
    }

    override fun getTootBarTitle(): String {
        return "ListView列表"
    }
}