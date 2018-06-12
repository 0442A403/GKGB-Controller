package com.iupp.iuppcontroller

import android.content.Context
import android.support.v7.widget.AppCompatButton

class SquareButton(context: Context): AppCompatButton(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}