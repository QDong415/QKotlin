package com.dq.qkotlin.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import com.dq.qkotlin.R

/**
 * 使用PagedListView 要保证PAGE_SIZE个item 一定可以充满屏幕
 */
class FooterListView : ListView {
    private var mEmptyView // 内容为空显示的emptyview。当无headerview时候使用，占满全屏
            : View? = null
    private var mEmptyFooterView // 有headerview时候使用，只是在底部
            : View? = null

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        initView(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        initView(context)
    }

    private fun initView(context: Context) {
        setFooterDividersEnabled(false)
    }

    fun resetFooterMessage(message: String?) {
        resetFooterMessageAndIcon(message, 0, 0)
    }

    @JvmOverloads
    fun resetFooterMessageAndIcon(
        message: String?,
        drawableIcon: Int,
        paddingBottomPx: Int = 0
    ) {
        if (mEmptyFooterView == null) {
            return
        }
        val tv =
            mEmptyFooterView!!.findViewById<View>(R.id.empty_tv) as TextView
        tv.text = message ?: ""
        if (drawableIcon != 0) {
            val drawable =
                resources.getDrawable(drawableIcon)
            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            tv.setCompoundDrawables(null, drawable, null, null)
        }
        if (paddingBottomPx != 0) {
            tv.setPadding(0, tv.paddingTop, 0, paddingBottomPx)
        }
    }

    fun removeEmptyFooter() {
        if (mEmptyFooterView != null) {
            removeFooterView(mEmptyFooterView)
            mEmptyFooterView = null
        }
    }

    fun setFooterViewBackgroundColor(color: Int) {
        mEmptyFooterView!!.setBackgroundColor(color)
    }

    override fun setEmptyView(newEmptyView: View) {
        // If we already have an Empty View, remove it
        if (mEmptyView != null) {
            if (mEmptyView === newEmptyView) {
                return
            }
            val currentEmptyViewParent = mEmptyView!!.parent
            if (null != currentEmptyViewParent
                && currentEmptyViewParent is ViewGroup
            ) {
                currentEmptyViewParent.removeView(mEmptyView)
            }
        }
        newEmptyView.let{
            newEmptyView.isClickable = true
            val newEmptyViewParent = newEmptyView.parent
            if (null != newEmptyViewParent
                && newEmptyViewParent is ViewGroup
            ) {
                newEmptyViewParent.removeView(newEmptyView)
            }
            (parent as ViewGroup).addView(
                newEmptyView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            super.setEmptyView(newEmptyView)
        }
        mEmptyView = newEmptyView
    }
}