package com.iupp.iuppcontroller

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.GridView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_controller.*


class ControllerActivity: Activity(), SocketCallback, OnTaskPressedListener {
    private var wifiSocket: WifiSocket? = null
    private var connectionSignal = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("IUPPInformation", "Controller activity has started with params: " +
                "host: ${intent.getStringExtra("host")}, " +
                "port: ${intent.getIntExtra("port", -1)}")
        wifiSocket = WifiSocket(intent.getStringExtra("host"),
                intent.getIntExtra("port", -1),
                this)
        wifiSocket!!.execute()
        setContentView(R.layout.activity_controller)
        val commands = arrayOf(
                Command("Sit down", Task.SitDown),
                Command("Stand Up", Task.StandUp),
                Command("Make a party", Task.MakeAParty),
                Command("Step forward", Task.StepForward),
                Command("Stop", Task.Stop)
        )
        buttonPanel.adapter = TaskButtonAdapter(commands, this@ControllerActivity, this)
        buttonPanel.numColumns = 2
        buttonPanel.verticalSpacing = 5
        buttonPanel.horizontalSpacing = 5
    }

    override fun onBackPressed() {
        wifiSocket?.disconnect()
        super.onBackPressed()
    }

    override fun onTaskPressed(task: Task) {
        while (!connectionSignal);
        Log.i("IUPPDebug", "New task: ${task.name}")
        wifiSocket!!.setTask(task)

    }

    override fun callback(code: SocketCode) {
        Log.i("IUPPInformation", "callback ${code.name}")
        runOnUiThread {
            if (code == SocketCode.ConnectionCompletedCode) {
                Toast.makeText(this, "Сокет успешно создан!", Toast.LENGTH_SHORT).show()
                connectionSignal = true
            } else if (code == SocketCode.RuntimeConnectionErrorCode
                    || code == SocketCode.DisconnectionCode) {
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