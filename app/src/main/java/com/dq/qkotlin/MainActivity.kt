package com.dq.qkotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.dq.qkotlin.ui.mvc.MvcRVFragment
import com.dq.qkotlin.ui.home.HomeFragment
import com.dq.qkotlin.ui.notifications.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navView: BottomNavigationView

    private lateinit var homeFragment: HomeFragment
    private lateinit var mvcRVFragment: MvcRVFragment
    private lateinit var profileFragment: ProfileFragment

    private var currentTabId  = 0//当前选中的tab id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createBottomNavigationView(savedInstanceState);
    }

    private fun createBottomNavigationView(savedInstanceState: Bundle?){
        navView = findViewById(R.id.nav_view)

        //设置导航栏菜单项Item选中监听
        navView.setOnNavigationItemSelectedListener { item ->
            val transaction = supportFragmentManager.beginTransaction()
            hideCurrentFragment(transaction)
            showFragment(item.itemId,transaction)
            currentTabId = item.itemId
            true
        }

        //通过TAG查找得到该Fragment，第一次还没添加的时候得到的fragment为null
        homeFragment = createFragment(HomeFragment::class.java, HomeFragment.TAG)
        mvcRVFragment = createFragment(MvcRVFragment::class.java, MvcRVFragment.TAG)
        profileFragment = createFragment(ProfileFragment::class.java, ProfileFragment.TAG)

        val transaction = supportFragmentManager.beginTransaction()
        // 因为在页面重启时，Fragment会被保存恢复，而此时再加载Fragment会重复加载，导致重叠
        if (savedInstanceState == null) {
            // 正常时候
            transaction.add(R.id.container, homeFragment, HomeFragment.TAG)
            transaction.commit()
            currentTabId = R.id.navigation_home
        } else {

            transaction.hide(homeFragment).hide(profileFragment).hide(mvcRVFragment)
            // “内存重启”时调用 解决重叠问题
            currentTabId = savedInstanceState.getInt("LAST_SELECT_TABID", R.id.navigation_home)
            showFragment(currentTabId,transaction)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存当前Fragment的下标
        outState.putInt("LAST_SELECT_TABID",navView.selectedItemId)
    }

    private fun <T> createFragment(cls: Class<T> ,tag: String) : T {
        if (supportFragmentManager.findFragmentByTag(tag) != null){
            return supportFragmentManager.findFragmentByTag(tag) as T
        } else {
            return cls.newInstance();
        }
    }

    //显示Fragment
    private fun showFragment(willTabId: Int, transaction: FragmentTransaction) {
        when (willTabId) {
            R.id.navigation_home -> {
                if (homeFragment.isAdded){
                    transaction.show(homeFragment)
                } else {
                    transaction.add(R.id.container, homeFragment, HomeFragment.TAG)
                }
            }
            R.id.navigation_dashboard -> {
                if (mvcRVFragment.isAdded){
                    transaction.show(mvcRVFragment)
                } else {
                    transaction.add(R.id.container, mvcRVFragment, MvcRVFragment.TAG)
                }
            }
            R.id.navigation_notifications -> {
                if (profileFragment.isAdded){
                    transaction.show(profileFragment)
                } else {
                    transaction.add(R.id.container, profileFragment, ProfileFragment.TAG)
                }
            }
        }
        transaction.commit()
    }

    //隐藏当前的Fragment，切换tab时候使用
    private fun hideCurrentFragment(transaction: FragmentTransaction) {
        when (currentTabId) {
            R.id.navigation_home -> transaction.hide(homeFragment)
            R.id.navigation_dashboard -> transaction.hide(mvcRVFragment)
            R.id.navigation_notifications -> transaction.hide(profileFragment)
        }
    }
}