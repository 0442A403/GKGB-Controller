package com.iupp.iuppcontroller

import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.Socket;
import java.net.InetSocketAddress
import java.util.*


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
                close()
                Log.e("IUPPError", "#233")
                callback.callback(SocketCode.ConnectionErrorCode)
                return null
            }
            callback.callback(SocketCode.ConnectionCompletedCode)
            if (checkConnection) {
                disconnect()
                return null
            }
            while (socket!!.isConnected && connection) {
                val data =
                        if (inStream!!.ready())
                            inStream!!.readLine()
                        else
                            null
                if (data != null)
                    Log.i("IUPPSocket", "Message: $data")
                if (actualTask != null) {
                    outStream!!.write(actualTask!!.command.toByteArray())
                    Log.i("IUPPSocket", "Command: ${actualTask!!.command}")
                    actualTask = null
                }
            }
        } catch (e: Exception) {
            callback.callback(SocketCode.ConnectionErrorCode)
        }
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

    private fun close() {
        socket?.close()
        inStream?.close()
        outStream?.close()
    }

    fun disconnect() {
        outStream!!.write(disconnectionMsg.toByteArray())
        close()
    }
}