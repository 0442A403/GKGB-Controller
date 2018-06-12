package com.iupp.iuppcontroller

import android.content.Context
import android.util.AttributeSet
import com.iupp.iuppcontroller.Joystick.Joystick

class HexapodJoystick: Joystick {
    constructor(context: Context?): super(context)
    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs)
    var stateAngle: Float? = null
    var lastOffset: Float? = null
    var lastDegrees: Float? = null
}