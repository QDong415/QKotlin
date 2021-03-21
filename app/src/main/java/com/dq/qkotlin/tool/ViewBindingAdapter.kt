package com.dq.qkotlin.tool

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

object ViewBindingAdapter {
    const val QINIU_URL = "https://qiniu.itopic.com.cn/"

    @JvmStatic //kotlin 必须加这句Static，java可以不加
    @BindingAdapter(value = ["key", "holder"], requireAll = false)
    fun setImageUri(
        imageView: ImageView,
        key: String,
        holder: Drawable
    ) {
        if (!TextUtils.isEmpty(key)){
            Glide.with(imageView.context)
                    .load(QINIU_URL + key)
                    .placeholder(holder)
                    .into(imageView)
        } else {
            imageView.setImageDrawable(holder)
        }
    }
}