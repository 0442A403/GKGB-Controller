package com.iupp.iuppcontroller

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_controller.*


class ControllerActivity: Activity(), SocketCallback {
    private var wifiSocket: WifiSocket? = null
    var connectionSignal = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiSocket = WifiSocket(intent.getStringExtra("host"),
                intent.getIntExtra("port", -1),
                this)
        wifiSocket!!.execute()
        setContentView(R.layout.activity_controller)
        val commands = arrayOf(Command("Sit", Task.Sit))
        buttonPanel.adapter = TaskButtonAdapter(commands, this)
        buttonPanel.setOnItemClickListener { adapterView, _, i, _ ->
            while (!connectionSignal)
            wifiSocket!!.setTask((adapterView.getItemAtPosition(i) as Command).task)
        }
    }

    override fun callback(code: SocketCode) {
        Log.i("IUPPInformation", "callback")
        if (code == SocketCode.ConnectionCompletedCode) {
            Toast.makeText(this, "Сокет успешно создан!", Toast.LENGTH_SHORT).show()
            connectionSignal = true
        } else if (code == SocketCode.RuntimeConnectionErrorCode
                || code == SocketCode.DisconnectedCode) {
            Toast.makeText(this, "Соединение разорвано", Toast.LENGTH_LONG).show()
            finish()
        } else if (code == SocketCode.TimeoutErrorCode
                || code == SocketCode.ConnectionErrorCode) {
            Toast.makeText(this, "Не удалось подключиться", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}