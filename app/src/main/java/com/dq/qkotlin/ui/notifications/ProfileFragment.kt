package com.dq.qkotlin.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dq.qkotlin.R
import com.dq.qkotlin.bean.UserBaseBean
import com.dq.qkotlin.tool.QApplication
import com.dq.qkotlin.ui.base.INavBar
import com.dq.qkotlin.ui.home.HomeViewModel
import com.kongzue.dialogx.dialogs.WaitDialog

class ProfileFragment : Fragment(), INavBar {

    companion object {
        val TAG = "ProfileFragment"
    }

    private lateinit var titleTextView: TextView

    private lateinit var profileViewModel: ProfileViewModel

    //AC.onCreate -> FM.onAttach -> FM.onCreate -> FM.onCreateView -> FM.onActivityCreated
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    //... -> FM.onCreateView -> FM.onActivityCreated -> FM.onStart -> AC.onStart -> AC.onResume -> FM.onResume
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        initView()
        initObservable()
    }

    private fun initView() {
        initStatusBar(this)
        initToolbarView(requireView().findViewById(R.id.view_stub_toolbar))

        titleTextView = requireView().findViewById<TextView>(R.id.home_tv)
        requireView().findViewById<View>(R.id.detail_button).setOnClickListener { v ->
            val params = HashMap<String, String>()
            params["to_userid"] = "1"
            profileViewModel.requestUserProfile(params)
        }
    }

    private fun initObservable() {
        //Dialog
        profileViewModel.loadingProfileLiveData
            .observe(viewLifecycleOwner, Observer<Boolean> { loading: Boolean ->
                if (loading) {
                    WaitDialog.show("MVVM写法加载中..")
                } else {
                    //关闭弹框
                    WaitDialog.dismiss()
                }
            })

        //Toast
        profileViewModel.toastLiveData
            .observe(viewLifecycleOwner, Observer<String> { toastMessage: String ->
                Toast.makeText(QApplication.instance, toastMessage, Toast.LENGTH_SHORT).show()
            })

        //数据
        profileViewModel.profileLiveData
            .observe(viewLifecycleOwner, Observer<UserBaseBean> { userBaseBean: UserBaseBean ->
                titleTextView.text = userBaseBean.name
            })
    }

    override fun getTootBarTitle(): String {
        return "MVVM+请求详情Model"
    }

    override fun getToolBarLeftIcon(): Int {
        return 0
    }
}