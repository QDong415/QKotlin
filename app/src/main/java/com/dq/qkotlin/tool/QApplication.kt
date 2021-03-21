package com.dq.qkotlin.tool

import android.app.Application
import android.util.Log

class QApplication : Application(){

    var inited : String = "notInit"

    override fun onCreate() {
        super.onCreate()
        inited = "initEd";
        Log.e("dz","QApplication onCreate");
    }
}