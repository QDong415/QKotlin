package com.dq.qkotlin.ui.home.detail

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.dq.qkotlin.R
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.databinding.ListitemFollowerBinding


class UserQuickAdapter(list: MutableList<UserBaseBean>)
    : BaseQuickAdapter<UserBaseBean, BaseDataBindingHolder<ListitemFollowerBinding>>(R.layout.listitem_follower,list) {

    // BaseRecyclerViewAdapterHelper官方的代码里有这么一句mPresenter，发现没什么用，就去掉了
    //    private val mPresenter: UserBaseBeanPresenter = UserBaseBeanPresenter()

    override fun convert(holder: BaseDataBindingHolder<ListitemFollowerBinding>, item: UserBaseBean) {

        //设置子View的点击事件
        // 获取 Binding
        val binding: ListitemFollowerBinding? = holder.dataBinding
        if (binding != null) {
            binding.setBean(item)
            //            binding.setPresenter(mPresenter)
            binding.executePendingBindings()

            //后半段是基于传统的，我建议还是用传统的方式去做
            binding.followButton.setText( if (item.follow == 1) "已经关注" else "未关注")

        } else {
            // BaseRecyclerViewAdapterHelper官方的代码里做了 != null 判断，但是我测试发现不可能为null
        }
    }
}