package com.iupp.iuppcontroller

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.Button

class TaskButton(private val view: ViewGroup): RecyclerView.ViewHolder(view) {
    val button = view.getChildAt(0) as Button
    fun setData(command: Command, callback: OnTaskPressedListener) {
        button.text = command.name
        button.setOnClickListener {
            callback.onTaskPressed(command.code)
        }
    }
}