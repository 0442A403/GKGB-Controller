package com.iupp.iuppcontroller
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.iupp.iuppcontroller.Joystick.JoystickListener
import kotlinx.android.synthetic.main.activity_controller.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class ControllerActivity : AppCompatActivity(), JoystickListener, OnTaskPressedListener,
        SocketCallback{
    private var wifiSocket: WifiSocket? = null
    private var connectionSignal = false
    var rotationAnimation: RotateAnimation? = null
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
        robotState.text = "Стою"
        joystick.setJoystickListener(this)
        val commandArray = arrayOf(
                Command("Сядь", SocketCode.SitDown),
                Command("Встань", SocketCode.StandUp),
                Command("Вечеринка", SocketCode.Party)
        )
        grid.adapter = TaskButtonAdapter(commandArray, this)
        grid.layoutManager = GridLayoutManager(this, 2)
    }

    override fun onTaskPressed(socketCode: SocketCode) {
        while (!connectionSignal);
        Log.i("IUPPDebug", "New task: ${socketCode.name}")
        wifiSocket!!.send(socketCode)
    }

    override fun callback(code: SocketCode) {
        Log.i("IUPPInformation", "callback ${code.name}")
        runOnUiThread {
            if (code == SocketCode.ConnectionCompleted) {
                Snackbar.make(controllerLayout, "Сокет успешно создан!", Snackbar.LENGTH_SHORT).show()
                connectionSignal = true
            } else if (code == SocketCode.RuntimeConnectionError
                    || code == SocketCode.Disconnection) {
                Snackbar.make(controllerLayout, "Соединение разорвано", Snackbar.LENGTH_LONG).show()
                finish()
            } else if (code == SocketCode.TimeoutError
                    || code == SocketCode.ConnectionError) {
                Snackbar.make(controllerLayout, "Не удалось подключиться", Snackbar.LENGTH_LONG).show()
                finish()
            }
            else {
                throw Exception("Unknown callback from socket")
            }
        }
    }

    override fun onDrag(degrees: Float, offset: Float) {
        Log.i("GKGBInformation", "onDrag; degree: $degrees $offset")
        joystick.lastOffset = offset
        joystick.lastDegrees = degrees
        if (offset < 0.75) {
            stateImageWrapper.visibility = View.INVISIBLE
            joystick.stateAngle = null
            rotationAnimation = null
            stateImage.setImageBitmap(null)
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
            if (stateImageWrapper.visibility != View.VISIBLE) {
                stateImageWrapper.visibility = View.VISIBLE
                stateImage.setImageResource(R.drawable.arrow)
            }
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
        if (joystick.lastOffset!! >= 0.75) {
            stateImage.setImageResource(R.drawable.arrow)
            robotState.text = "Иду"
            val angle = when (joystick.stateAngle!!) {
                60f -> 30
                0f -> 90
                -60f -> 150
                -120f -> 210
                -180f -> 270
                180f -> 270
                else -> 330
            } * PI / 180
            Log.i("GKGBInformation", "degree: $angle")
            joystick.setCenter((cos(angle) * joystick.radius * 0.9).toInt(),
                    -(sin(angle) * joystick.radius * 0.9).toInt())
        }
        else {
            stateImage.setImageBitmap(null)
            stateImageWrapper.visibility = View.INVISIBLE
            robotState.text = "Стою"
        }
        rotationAnimation = null
    }
}
