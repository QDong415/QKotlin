package com.dq.qkotlin.ui.mvc

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dq.qkotlin.R
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.tool.dp2px


class FriendRecyclerAdapter(val context: Context, val list: List<UserBaseBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    //是否可以显示emptyView。因为刚进来第一次加载中的时候，虽然list是empty，但我不希望显示emptyView
    var showEmptyViewEnable = false

    //viewType分别为item以及空view
    private val VIEW_TYPE_ITEM: Int = 0
    private val VIEW_TYPE_EMPTY: Int = 1

    //item上的控件的点击事件
    private var onItemClickListener: OnItemClickListener? = null

    //itemview数量
    override fun getItemCount(): Int {
        //这里也需要添加判断，如果list.size()为0的话，只引入一个布局，就是emptyView，此时 这个recyclerView的itemCount为1
        if (showEmptyViewEnable && list.isNullOrEmpty()) {
            return 1;
        }
        //如果不为0，按正常的流程跑
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        //在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
        return if (showEmptyViewEnable && list.isNullOrEmpty()) {
            VIEW_TYPE_EMPTY
        } else VIEW_TYPE_ITEM
        //如果有数据，则使用ITEM的布局
    }

    //创建ViewHolder并绑定上itemview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //在这里根据不同的viewType进行引入不同的布局
        if (viewType == VIEW_TYPE_EMPTY) {
            //空布局
            val emptyView: View = mInflater.inflate(R.layout.listview_empty, parent, false)
            return object : RecyclerView.ViewHolder(emptyView) {}
        } else {
            //正常item
            val view: View = mInflater.inflate(R.layout.listitem_follower, parent, false)
            val viewHolder = TopicViewHolder(view)
            viewHolder.itemView.setOnClickListener {
                onItemClickListener?.onItemClick(viewHolder.itemView.tag as Int);
            }
            return viewHolder
        }
    }

    //ViewHolder的view控件设置数据
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TopicViewHolder) {
            holder.name_tv.text = list.get(position).name
            holder.itemView.tag = position

            Glide.with(holder.avatar_iv.context)//.context是MainActivity
                .load(list.get(position).avatar)
                .placeholder(R.drawable.user_photo)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(dp2px(holder.avatar_iv.context ,4))))
                .into(holder.avatar_iv)
        }
    }


    //kotlin 内部类默认是static ,前面加上inner为非静态
    //自定义的RecyclerView.ViewHolder，构造函数需要传入View参数。相当于java的构造函数第一句的super(view);
    class TopicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name_tv: TextView = view.findViewById(R.id.name_tv)
        val avatar_iv: ImageView = view.findViewById(R.id.avatar_iv)

//        init {
//            name_tv.setTextColor(view.resources.getColor(R.color.sky_color))
//        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

}