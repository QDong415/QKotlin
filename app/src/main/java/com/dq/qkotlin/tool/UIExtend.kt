package com.dq.qkotlin.tool

import android.content.Context
import android.util.TypedValue


fun dp2px(context : Context, dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
        context.getResources().getDisplayMetrics()).toInt()
}