package com.iupp.iuppcontroller
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.iupp.iuppcontroller.Joystick.JoystickListener
import kotlinx.android.synthetic.main.activity_controller.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class ControllerActivity : AppCompatActivity(),
        JoystickListener, OnTaskPressedListener, SocketCallback{

    private var wifiSocket: WifiSocket? = null
    private var connectionSignal = false
    private var rotationAnimation: RotateAnimation? = null
    private var state = State.Staying
    private var picking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("GKGBInformation", "Controller activity has started with params: " +
                "host: ${intent.getStringExtra("host")}, " +
                "port: ${intent.getIntExtra("port", -1)}")
        wifiSocket = WifiSocket(intent.getStringExtra("host"),
                intent.getIntExtra("port", -1), this)
        wifiSocket!!.execute()
        setContentView(R.layout.activity_controller)
        robotState.text = "Стою"
        joystick.setJoystickListener(this)
        val commandArray = arrayOf(
                Command("Повернись влево", SocketCode.TurnClockwise),
                Command("Повернись вправо", SocketCode.TurnCounterclockwise),
                Command("Сядь", SocketCode.SitDown),
                Command("Встань", SocketCode.StandUp),
                Command("Вечеринка", SocketCode.Party),
                Command("Тест", SocketCode.Test)
        )
        grid.adapter = TaskButtonAdapter(commandArray, this)
        grid.layoutManager = GridLayoutManager(this, 2)
    }

    override fun onTaskPressed(socketCode: SocketCode) {
        while (!connectionSignal);
        wifiSocket!!.send(socketCode)
        setDefaultJoystickState()
        joystick.setCenter(0, 0)
        stateImage.setImageResource(R.drawable.red_circle)
        when (socketCode) {
            SocketCode.StandUp -> {
                state = State.Staying
                robotState.text = "Стою"
            }
            SocketCode.SitDown -> {
                state= State.Sitting
                robotState.text = "Сижу"
            }
            SocketCode.Party -> {
                state = State.Staying
                robotState.text = "Вечеринка"
            }
            SocketCode.TurnCounterclockwise, SocketCode.TurnClockwise -> {
                state = State.Turning
                robotState.text = "Поворачиваюсь"
            }
            else -> {}
        }
    }

    private fun setDefaultJoystickState() {
        joystick.stateAngle = null
        rotationAnimation = null
    }

    override fun callback(code: SocketCode) {
        Log.i("GKGBInformation", "Callback ${code.name}")
        runOnUiThread {
            Snackbar.make(controllerLayout, "Callback: ${code.name}", Snackbar.LENGTH_LONG)
            when (code) {
                SocketCode.ConnectionCompleted -> {
                    Snackbar.make(controllerLayout, "Сокет успешно создан!", Snackbar.LENGTH_SHORT).show()
                    connectionSignal = true
                }
                SocketCode.RuntimeConnectionError, SocketCode.Disconnection -> {
                    Snackbar.make(controllerLayout, "Соединение разорвано", Snackbar.LENGTH_LONG).show()
                    wifiSocket!!.disconnect()
                    finish()
                }
                SocketCode.TimeoutError, SocketCode.ConnectionError -> {
                    Snackbar.make(controllerLayout, "Не удалось подключиться", Snackbar.LENGTH_LONG).show()
                    wifiSocket!!.disconnect()
                    finish()
                }
                SocketCode.CompletingTask -> {
                    if (state == State.Staying || state == State.Sitting)
                        stateImage.setImageResource(R.drawable.green_circle)
                    else
                        stateImage.setImageResource(R.drawable.arrow_green)
                }
                else -> {
                    throw Exception("Unknown callback from socket")
                }
            }
        }
    }

    override fun onDrag(degrees: Float, offset: Float) {
        Log.i("GKGBInformation", "onDrag; degree: $degrees $offset")
        joystick.lastOffset = offset
        joystick.lastDegrees = degrees
        if (offset < 0.75) {
            setDefaultJoystickState()
            picking = false
        }
        else if (rotationAnimation == null || rotationAnimation!!.hasEnded()) {
            val newAngle = when (degrees) {
                in 0f..60f -> 60f
                in 60f .. 120f -> 0f
                in 120f .. 180f -> -60f
                in -60f .. 0f -> {
                    if (joystick.stateAngle == -180f)
                        joystick.stateAngle = 180f
                    120f
                }
                in -180f .. -120f -> {
                    if (joystick.stateAngle == 180f)
                        joystick.stateAngle = -180f
                    -120f

                }
                else -> {
                    if (joystick.stateAngle == null || joystick.stateAngle!! > 0)
                        180f
                    else
                        -180f
                }
            }
            if (joystick.stateAngle == null) {
                rotationAnimation = RotateAnimation(0f, newAngle,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f)
                rotationAnimation!!.duration = 0
            }
            else {
                rotationAnimation = RotateAnimation(joystick.stateAngle!!, newAngle,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f)
                rotationAnimation!!.duration = 75
            }
            if (!picking)
                stateImage.setImageResource(R.drawable.arrow_orange)
            picking = true
            rotationAnimation!!.interpolator = LinearInterpolator()
            rotationAnimation!!.fillAfter = true
            stateImage.startAnimation(rotationAnimation)
            joystick.stateAngle = newAngle
        }
    }

    override fun onDown() {
        Log.i("GKGBInformation", "onDown")
    }

    override fun onUp() {
        Log.i("GKGBInformation", "onUp")
        picking = false
        if (joystick.lastOffset!! >= 0.75) {
            stateImage.setImageResource(R.drawable.arrow_light_orange)
            robotState.text = "Иду"
            state = State.Walking
            val angle = when (joystick.stateAngle!!) {
                60f -> {
                    wifiSocket!!.send(SocketCode.MoveRightForward)
                    30
                }
                0f -> {
                    wifiSocket!!.send(SocketCode.MoveForward)
                    90
                }
                -60f -> {
                    wifiSocket!!.send(SocketCode.MoveLeftForward)
                    150
                }
                -120f -> {
                    wifiSocket!!.send(SocketCode.MoveLeftBack)
                    210
                }
                -180f -> {
                    wifiSocket!!.send(SocketCode.MoveBack)
                    270
                }
                180f -> {
                    wifiSocket!!.send(SocketCode.MoveBack)
                    270
                }
                else -> {
                    wifiSocket!!.send(SocketCode.MoveRightBack)
                    330
                }
            } * PI / 180
            joystick.setCenter((cos(angle) * joystick.radius * 0.9).toInt(),
                    -(sin(angle) * joystick.radius * 0.9).toInt())
        }
        else {
            setDefaultJoystickState()
            joystick.setCenter(0, 0)
            stateImage.setImageResource(R.drawable.green_circle)
            robotState.text = "Стою"
            state = State.Staying
            wifiSocket!!.send(SocketCode.StandUp)
        }
        rotationAnimation = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        wifiSocket?.disconnect()
    }

    private enum class State {
        Walking, Staying, Sitting, Turning
    }
}
