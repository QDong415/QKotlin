package com.dq.qkotlin.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.dq.qkotlin.R
import com.zackratos.ultimatebarx.library.UltimateBarX


/**
 * 基础页面
 * @author QDong
 */
abstract class BaseFragment : Fragment() {

    protected var mToolbar: Toolbar? = null
    protected lateinit var mViewStubToolbar: ViewStub

    private var isViewCreated = false
    private var isViewVisable = false

    open fun initCommonView(view: View) {
        mViewStubToolbar = view.findViewById(R.id.view_stub_toolbar)
        mViewStubToolbar.let {
            if (enableToolbar()) {
                mViewStubToolbar.layoutResource = onBindToolbarLayout()
                mToolbar = mViewStubToolbar.inflate().findViewById(R.id.toolbar_root)
                mToolbar?.title = getTootBarTitle()
//                setTitleCenter(mToolbar!!)
                if (enableToolBarLeft()) {
                    mToolbar?.setNavigationIcon(getToolBarLeftIcon())
                    mToolbar?.setNavigationOnClickListener { (activity as AppCompatActivity).onBackPressed() }
                } else {
                    mToolbar?.setNavigationIcon(null)
                }
            }
        }
    }

    //把状态栏的标题居中
    fun setTitleCenter(toolbar: Toolbar) {
        val textView = toolbar.getChildAt(0) as TextView //主标题
        textView.gravity = Gravity.CENTER
        val params =
            Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.MATCH_PARENT
            )
        params.gravity = Gravity.CENTER
        textView.layoutParams = params
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true
    }

    /**
     * isVisibleToUser =true的时候代表当前页面可见，false 就是不可见
     * setUserVisibleHint(boolean isVisibleToUser) 是在 Fragment OnCreateView()方法之前调用的
     * 注：FragmentTransaction.setMaxLifecycle 处理 Lifecycle.State.RESUMED 则此函数不进行回调，失效
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isViewVisable = isVisibleToUser
        //如果启用了懒加载就进行懒加载，
        if (enableLazyData() && isViewVisable) {
            lazyLoad()
        }
    }

    /**
     * 懒加载机制 当页面可见的时候加载数据
     * 如果当前 FragmentTransaction.setMaxLifecycle 处理 Lifecycle.State.RESUMED 则 懒加载失效
     * 如果 FragmentTransaction.setMaxLifecycle 传入BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT ，
     * 则只有当前的Fragment处于Lifecycle.State.RESUMED状态。 所有其他片段的上限为Lifecycle.State.STARTED 。
     * 如果传递了BEHAVIOR_SET_USER_VISIBLE_HINT ，则所有片段都处于Lifecycle.State.RESUMED状态，
     * 并且将存在Fragment.setUserVisibleHint(boolean)回调
     */
    private fun lazyLoad() {
        //这里进行双重标记判断,必须确保onCreateView加载完毕且页面可见,才加载数据
        if (isViewCreated && isViewVisable) {
            initData()
            //数据加载完毕,恢复标记,防止重复加载
            isViewCreated = false
            isViewVisable = false
        }
    }

    //默认启用懒加载
    open fun enableLazyData(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        //如果启用了懒加载就进行懒加载，否则就进行预加载
        Log.e("dz","BaseFrag onResume")
        if (enableLazyData()) {
            lazyLoad()
        } else {
            initData()
        }
    }

    // abstract fun initView(mView: View)
    abstract fun initData()

    open fun getTootBarTitle(): String {
        return ""
    }

    /**
     * 设置返回按钮的图样，可以是Drawable ,也可以是ResId
     * 注：仅在 enableToolBarLeft 返回为 true 时候有效
     *
     * @return
     */
    open fun getToolBarLeftIcon(): Int {
        return R.drawable.ic_white_black_24dp
    }

    /**
     * 是否打开返回
     * @return
     */
    open fun enableToolBarLeft(): Boolean {
        return false
    }

    open fun enableToolbar(): Boolean {
        return true
    }

    //toolbar用什么布局
    open fun onBindToolbarLayout(): Int {
        return R.layout.common_toolbar
    }

    open fun startActivity(clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(activity, clz)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
    }

    /**
     * 跳转方式
     * 使用方法 startActivity<TargetActivity> { putExtra("param1", "data1") }
     */
    inline fun <reified T> startActivity(block: Intent.() -> Unit) {
        val intent = Intent(context, T::class.java)
        intent.block()
        startActivity(intent)
    }

    private var mLastButterKnifeClickTime: Long = 0

    /**
     * 是否快速点击
     * @return true 是
     */
    open fun beFastClick(): Boolean {
        val currentClickTime = System.currentTimeMillis()
        val flag = currentClickTime - mLastButterKnifeClickTime < 400L
        mLastButterKnifeClickTime = currentClickTime
        return flag
    }
}