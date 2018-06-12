package com.iupp.iuppcontroller

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

class TaskButtonAdapter(private val commands: Array<Command>,
                        private val onTaskPressedListener: OnTaskPressedListener): RecyclerView.Adapter<TaskButton>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskButton {
        val view = (LayoutInflater
                .from(parent.context)
                .inflate(R.layout.task_button, parent, false) as ViewGroup)
        return TaskButton(view)
    }

    override fun getItemCount(): Int = commands.size

    override fun onBindViewHolder(holder: TaskButton, position: Int) {
        holder.setData(commands[position], onTaskPressedListener)
    }
}

