package com.dq.qkotlin.ui.base

import android.content.Intent
import android.util.Log
import android.view.ViewStub
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dq.qkotlin.R
import com.zackratos.ultimatebarx.library.UltimateBarX

open class BaseAppCompatActivity : AppCompatActivity() {

    open fun initView(){
        initToolbarView()
        initStatusBarView()
    }

    open fun initToolbarView() {
        var mViewStubToolbar: ViewStub? = findViewById(R.id.view_stub_toolbar)

        mViewStubToolbar?.let {
            if (enableToolbar()) {
                it!!.layoutResource = onBindToolbarLayout()
                var mToolbar: Toolbar = it!!.inflate().findViewById(R.id.toolbar_root)
                mToolbar?.title = getTootBarTitle()
//                setTitleCenter(mToolbar!!) 标题居中
                mToolbar?.setNavigationIcon(getToolBarLeftIcon())
                mToolbar?.setNavigationOnClickListener { onBackPressed() }
            }
        }
    }

    open fun initStatusBarView() {
        UltimateBarX.with(this)
                .fitWindow(true) //布局是否侵入状态栏（true 不侵入，false 侵入）
                .colorRes(R.color.toolbar_color)// 状态栏背景颜色（色值）
                .light(true) // 状态栏字体 true: 灰色，false: 白色 Android 6.0+
                .applyStatusBar()// 应用到状态栏
    }

    open fun getTootBarTitle(): String {
        return ""
    }

    /**
     * 设置返回按钮的图样，可以是Drawable ,也可以是ResId
     * 注：仅在 enableToolBarLeft 返回为 true 时候有效
     */
    open fun getToolBarLeftIcon(): Int {
        return R.drawable.ic_white_black_24dp
    }

    open fun enableToolbar(): Boolean {
        return true
    }

    //toolbar用什么布局
    open fun onBindToolbarLayout(): Int {
        return R.layout.common_toolbar
    }

    /**
     * 跳转方式
     * 使用方法 startActivity<TargetActivity> { putExtra("param1", "data1") }
     */
    inline fun <reified T> startActivity(block: Intent.() -> Unit) {
        val intent = Intent(this, T::class.java)
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