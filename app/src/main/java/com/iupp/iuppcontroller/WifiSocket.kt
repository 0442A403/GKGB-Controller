package com.iupp.iuppcontroller

import android.os.AsyncTask
import android.util.Log
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader
import java.io.OutputStream;
import java.io.Serializable
import java.net.InetSocketAddress

class WifiSocket(private val host: String,
                 private val port: Int,
                 private var callback: SocketCallback,
                 private val checkConnection: Boolean = false): AsyncTask<Void, String, Void>(), Serializable {
    private val timeout = 5000
    private var socket: Socket? = null
    private var inStream: BufferedReader? = null
    private var outStream: OutputStream? = null
    private val pingMsg = "SOCKET_PING"
    private val connectedMsg = "SOCKET_CONNECTED"
    private val disconnectedMsg = "SOCKET_DISCONNECTED"
    private var connectionSignal = false
    private var actualTask: Task? = null
    override fun doInBackground(vararg p0: Void?): Void? {
        try {
            socket = Socket()
            socket!!.connect(InetSocketAddress(host, port), timeout)

            inStream = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            outStream = socket!!.getOutputStream()

            if (socket!!.isConnected) {
                val startTime = System.currentTimeMillis()
                while (!inStream!!.ready()) {
                    if (System.currentTimeMillis() - startTime > timeout) {
                        callback.callback(SocketCode.TimeoutErrorCode)
                        return null
                    }
                }
                connectionSignal = true
            }
            else {
                callback.callback(SocketCode.ConnectionErrorCode)
                return null
            }
            while (connectionSignal && !checkConnection) {
                val msg = inStream!!.readLine()
                onProgressUpdate(msg)
                if (actualTask != null) {
                    outStream!!.write(actualTask!!.code)
                    actualTask = null
                }
            }
        } catch (e: Exception) {
            callback.callback(SocketCode.ConnectionErrorCode)
        }
        if (socket != null)
            socket!!.close()
        if (inStream != null)
            inStream!!.close()
        if (outStream != null)
            outStream!!.close()
        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        super.onProgressUpdate(*values)
        if (values.isEmpty())
            return
        val msg = values[0]
        when (msg) {
            connectedMsg -> callback.callback(SocketCode.ConnectionCompletedCode)
            disconnectedMsg -> {
                callback.callback(SocketCode.DisconnectedCode)
                connectionSignal = false
            }
        }
        Log.i("SocketInformation", values[0])
    }

    fun setTask(task: Task) {
        actualTask = task
    }
}