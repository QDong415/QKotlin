package com.dq.qkotlin.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import com.dq.qkotlin.BR
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.databinding.ListitemFollowerBinding

//基于DataBindingUtil的 ListView Adapter，但是下半段展示的是基于传统的方式
class HomeListViewAdapter(val context: Context, var list: MutableList<UserBaseBean>
                          , @LayoutRes private val layoutResId: Int) : BaseAdapter() {

    private var onItemClickListener: OnItemClickListener? = null

    override fun getCount(): Int = list.size

    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int,convertView: View?, parent: ViewGroup?): View {
        //前半段是基于ViewBinding绑定数据，虽然简化很多 但是有一些弊端：比如自定义显示条件的时候需要新增很多类且别人不容易找到对应的类，且xml不方便复用
       val binding: ListitemFollowerBinding
       if (convertView == null){
           binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    layoutResId ,parent,false)
           binding.followButton.setOnClickListener { v ->
               onItemClickListener?.onItemClick(v ,v.tag as Int);
           }
           binding.deleteButton.setOnClickListener { v ->
               onItemClickListener?.onItemClick(v ,v.tag as Int);
           }
        } else {
           binding = DataBindingUtil.getBinding(convertView)!!
        }
        binding.setVariable(BR.bean, list[position])

        //后半段是基于传统的，我建议还是用传统的方式去做
        binding.followButton.tag = position
        binding.followButton.setText( if (list[position].follow == 1) "已经关注" else "未关注")

        binding.deleteButton.tag = position

        return binding.root
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(view: View ,position: Int)
    }
}