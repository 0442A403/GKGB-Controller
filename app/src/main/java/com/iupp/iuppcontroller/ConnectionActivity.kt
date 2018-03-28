package com.iupp.iuppcontroller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_connection.*

class ConnectionActivity : AppCompatActivity(), SocketCallback {
    private var host: String? = null
    private var port: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        connect_button.setOnClickListener {
            connect(host_editText.text.toString(),
                    port_editText.text.toString())
        }
    }

    override fun callback(code: SocketCode) {
        if (code == SocketCode.ConnectionCompletedCode)
            runOnUiThread {
                startActivity(
                        Intent(this, ControllerActivity::class.java)
                                .putExtra("host", host!!)
                                .putExtra("port", port!!)
                )
            }
        else if (code == SocketCode.ConnectionErrorCode
                || code == SocketCode.TimeoutErrorCode
                || code == SocketCode.RuntimeConnectionErrorCode) {
            runOnUiThread {
                Toast.makeText(this@ConnectionActivity, "Не удалось подключиться", Toast.LENGTH_LONG).show()
            }
            connect_button.isClickable = true
        }
    }

    private fun connect(host: String, port: String) {
        if (host.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, "Не удалось подключиться", Toast.LENGTH_SHORT).show()
            return
        }
        connect_button.isClickable = false
        this.host = host
        this.port = port.toInt()
        WifiSocket(host, port.toInt(), this, true).execute()
    }
}
