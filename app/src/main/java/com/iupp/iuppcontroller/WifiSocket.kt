package com.iupp.iuppcontroller

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.Serializable
import java.net.InetSocketAddress
import java.net.Socket


class WifiSocket(private val host: String,
                 private val port: Int,
                 private val callback: SocketCallback,
                 private val checkConnection: Boolean = false): AsyncTask<Void, SocketCode, Void>(), Serializable {
    private val timeout = 2000
    private var socket: Socket = Socket()
    private var inStream: BufferedReader? = null
    private var outStream: OutputStream? = null
    private var connection = true
    private var actualTask: SocketCode? = null
    override fun doInBackground(vararg p0: Void?): Void? {
        try {
            Log.i("IUPPSocket", "Creating socket with params: host - $host, port - $port")
            socket.connect(InetSocketAddress(host, port), timeout)

            inStream = BufferedReader(InputStreamReader(socket.getInputStream()))
            outStream = socket.getOutputStream()

            if (!socket.isConnected) {
                close()
                callback.callback(SocketCode.ConnectionError)
                return null
            }
            callback.callback(SocketCode.ConnectionCompleted)
            if (checkConnection) {
                disconnect()
                return null
            }
            while (socket.isConnected && connection) {
                val data =
                        if (inStream!!.ready())
                            inStream!!.readLine()
                        else
                            null
                if (data != null) {
                    Log.i("IUPPSocket", "Message: $data")
                    onProgressUpdate(SocketCode.valueOf(data))
                }
                if (data != null) {
                    outStream!!.write(actualTask!!.name.toByteArray())
                    Log.i("IUPPSocket", "Command: ${actualTask!!.name}")
                    actualTask = null
                }
            }
        } catch (e: Exception) {
            callback.callback(SocketCode.ConnectionError)
        }
        return null
    }

    override fun onProgressUpdate(vararg values: SocketCode?) {
        super.onProgressUpdate(*values)
        if (values.isEmpty())
            return
        val msg = values[0]!!
        when (msg) {
            SocketCode.ConnectionCompleted -> {
                callback.callback(SocketCode.ConnectionCompleted)
                return
            }
            SocketCode.Disconnection -> {
                callback.callback(SocketCode.Disconnection)
                connection = false
                return
            }
        }
    }

    fun send(socketCode: SocketCode) {
        actualTask = socketCode
    }

    private fun close() {
        socket.close()
        inStream?.close()
        outStream?.close()
    }

    fun disconnect() {
        outStream!!.write(SocketCode.Disconnection.name.toByteArray())
        close()
    }
}