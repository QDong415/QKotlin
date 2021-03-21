package com.yang.databindingdemo.listView.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.LayoutRes

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.dq.qkotlin.BR
import com.dq.qkotlin.bean.UserBaseBean

class ListViewAdapter(val context: Context ,var list: MutableList<UserBaseBean>
                      ,@LayoutRes private val layoutResId: Int) : BaseAdapter() {

    override fun getCount(): Int = list.size

    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int,convertView: View?, parent: ViewGroup?): View {
       val binding: ViewDataBinding = if (convertView == null){
            DataBindingUtil.inflate(LayoutInflater.from(context),
                    layoutResId ,parent,false)
        } else {
            DataBindingUtil.getBinding(convertView)!!
        }
        binding.setVariable(BR.bean, list[position])
        return binding.root
    }
}