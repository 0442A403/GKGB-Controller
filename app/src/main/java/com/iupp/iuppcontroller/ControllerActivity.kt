package com.iupp.iuppcontroller


//class ControllerActivity: AppCompatActivity(), SocketCallback, OnTaskPressedListener {
//    private var wifiSocket: WifiSocket? = null
//    private var connectionSignal = false
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.i("IUPPInformation", "Controller activity has started with params: " +
//                "host: ${intent.getStringExtra("host")}, " +
//                "port: ${intent.getIntExtra("port", -1)}")
//        wifiSocket = WifiSocket(intent.getStringExtra("host"),
//                intent.getIntExtra("port", -1),
//                this)
//        wifiSocket!!.execute()
//        setContentView(R.layout.activity_controller)
//        val commands = arrayOf(
//                Command("Sit down", Task.SitDown),
//                Command("Stand Up", Task.StandUp),
//                Command("Make a party", Task.MakeAParty),
//                Command("Step forward", Task.StepForward),
//                Command("Stop", Task.Stop)
//        )
////        buttonPanel.adapter = TaskButtonAdapter(commands, this@ControllerActivity, this)
////        buttonPanel.numColumns = 2
////        buttonPanel.verticalSpacing = 5
////        buttonPanel.horizontalSpacing = 5
//    }
//
//    override fun onBackPressed() {
//        wifiSocket?.disconnect()
//        super.onBackPressed()
//    }
//
//    override fun onTaskPressed(task: Task) {
//        while (!connectionSignal);
//        Log.i("IUPPDebug", "New task: ${task.name}")
//        wifiSocket!!.setTask(task)
//
//    }
//
//    override fun callback(code: SocketCode) {
//        Log.i("IUPPInformation", "callback ${code.name}")
//        runOnUiThread {
//            if (code == SocketCode.ConnectionCompleted) {
//                Toast.makeText(this, "Сокет успешно создан!", Toast.LENGTH_SHORT).show()
//                connectionSignal = true
//            } else if (code == SocketCode.RuntimeConnectionError
//                    || code == SocketCode.Disconnection) {
//                Toast.makeText(this, "Соединение разорвано", Toast.LENGTH_LONG).show()
//                finish()
//            } else if (code == SocketCode.TimeoutError
//                    || code == SocketCode.ConnectionError) {
//                Toast.makeText(this, "Не удалось подключиться", Toast.LENGTH_LONG).show()
//                finish()
//            }
//        }
//    }
//}

