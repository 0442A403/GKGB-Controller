package com.iupp.iuppcontroller

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
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
        buttonPanel.setSettings()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        wifiSocket?.disconnect()
    }

    private fun GridView.setSettings() {
        val commands = arrayOf(Command("Sit", Task.Sit))
        this.adapter = TaskButtonAdapter(commands, this@ControllerActivity)
        this.setOnItemClickListener { adapterView, _, i, _ ->
            while (!connectionSignal)
                wifiSocket!!.setTask((adapterView.getItemAtPosition(i) as Command).task)
        }
        this.numColumns = 3
        this.verticalSpacing = 5
        this.horizontalSpacing = 5
    }

    override fun callback(code: SocketCode) {
        Log.i("IUPPInformation", "callback")
        runOnUiThread {
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
}