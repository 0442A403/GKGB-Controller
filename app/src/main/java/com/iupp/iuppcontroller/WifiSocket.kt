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
    private val timeout = 2000
    private var socket: Socket? = null
    private var inStream: BufferedReader? = null
    private var outStream: OutputStream? = null
    private val pingMsg = "SOCKET_PING"
    private val connectedMsg = "SOCKET_CONNECTED"
    private val disconnectionMsg = "DISCONNECTION"
    private var connection = true
    private var actualTask: Task? = null
    override fun doInBackground(vararg p0: Void?): Void? {
        try {
            socket = Socket()
            Log.i("IUPPSocket", "Creating socket with params: host - $host, port - $port")
            socket!!.connect(InetSocketAddress(host, port), timeout)

            inStream = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            outStream = socket!!.getOutputStream()

            if (!socket!!.isConnected) {
                socket!!.close()
                inStream!!.close()
                outStream!!.close()
                callback.callback(SocketCode.ConnectionErrorCode)
                return null
            }
            if (checkConnection) {
                outStream!!.write(disconnectionMsg.toByteArray())
                Log.i("IUPPDebug", "1")
            }
            Log.i("IUPPDebug", "2")
            while (socket!!.isConnected && connection) {
                Log.i("IUPPDebug", "3")
                val data = inStream!!.readLine()
                Log.i("IUPPSocket", "Message: $data")
                if (checkConnection && data.toString() == disconnectionMsg) {
                    callback.callback(SocketCode.ConnectionCompletedCode)
                    break
                }
                if (actualTask != null) {
                    outStream!!.write(actualTask!!.code)
                    actualTask = null
                }
            }
        } catch (e: Exception) {
            callback.callback(SocketCode.ConnectionErrorCode)
        }
        socket?.close()
        inStream?.close()
        outStream?.close()
        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        super.onProgressUpdate(*values)
        if (values.isEmpty())
            return
        val msg = values[0]!!
        when (msg) {
            connectedMsg -> {
                callback.callback(SocketCode.ConnectionCompletedCode)
                return
            }
            disconnectionMsg -> {
                callback.callback(SocketCode.DisconnectionCode)
                connection = false
                return
            }
        }
    }

    fun setTask(task: Task) {
        actualTask = task
    }

    fun disconnect() {
        socket!!.close()
    }
}