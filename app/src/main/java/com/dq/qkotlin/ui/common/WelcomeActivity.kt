package com.dq.qkotlin.ui.common

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.dq.qkotlin.MainActivity
import com.dq.qkotlin.ui.base.INavBar
import java.util.*

class WelcomeActivity : AppCompatActivity() ,INavBar {

    private val handler = Handler(Looper.getMainLooper())

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initStatusBar(this)

        //延时是为了让onCreate先走完，耗时代码放到Runnable里，这样可以防止耗时代码导致的白屏
        handler.postDelayed(initRun, 150)
    }

    private val initRun = Runnable {
        //这里可以写耗时操作，比如读取数据，初始化表情，读取缓存，设置全局变量等

        //1.3秒后跳转到主页 或者 登录页，看具体需求
        handler.postDelayed(launchHome, 1300)
    }

    private val launchHome = Runnable {
        val intent = Intent()
        intent.setClass(this@WelcomeActivity, MainActivity::class.java)
//        if (AccountManager.instance.isLogin){
//            intent.putExtra("relink", intent.getIntExtra("relink", -1))
//            intent.setClass(this@WelcomeActivity, MainActivity::class.java)
//        } else {
//            intent.setClass(this@WelcomeActivity, UserRegWechatActivity::class.java)
//        }
        startActivity(intent)
        finish()
    }
}