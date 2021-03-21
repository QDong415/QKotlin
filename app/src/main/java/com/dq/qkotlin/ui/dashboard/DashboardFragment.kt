package com.dq.qkotlin.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dq.qkotlin.R
import com.zackratos.ultimatebarx.library.UltimateBarX

class DashboardFragment : Fragment() {

    companion object {
        val TAG = "DashboardFragment"
    }

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var textView: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        textView = root.findViewById(R.id.text_dashboard)
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        Log.e("dz","onCreateView");

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.e("dz","onActivityCreated");

//        UltimateBarX.with(this)
//                .fitWindow(true) //布局是否侵入状态栏（true 不侵入，false 侵入）
//                .colorRes(R.color.toolbar_color)// 状态栏背景颜色（色值）
//                .light(true) // 状态栏字体 true: 灰色，false: 白色 Android 6.0+
//                .applyStatusBar()// 应用到状态栏

        textView.setOnClickListener { v->
            Log.e("dz","v->Click");
            dashboardViewModel.requireUserList();
         }
    }
}