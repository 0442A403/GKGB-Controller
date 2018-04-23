package com.iupp.iuppcontroller

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.Toast

class TaskButtonAdapter(private val commands: Array<Command>,
                        private val context: Context,
                        private val onTaskPressedListener: OnTaskPressedListener): BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val button = Button(context)
        button.text = commands[p0].name
        button.setOnClickListener {
            onTaskPressedListener.onTaskPressed(commands[p0].task)
        }
        return button
    }

    override fun getItem(p0: Int): Any? {
        return commands[p0]
    }

    override fun getItemId(p0: Int): Long {
        return commands[p0].task.command.hashCode().toLong()
    }

    override fun getCount(): Int {
        return commands.size
    }
}

