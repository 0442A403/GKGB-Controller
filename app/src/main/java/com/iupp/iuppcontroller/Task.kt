package com.iupp.iuppcontroller

enum class Task(val command: String) {
    SitDown("COMMAND_SIT_DOWN"),
    StandUp("COMMAND_STAND_UP"),
    MakeAParty("COMMAND_MAKE_PARTY"),
    Stop("COMMAND_STOP"),
    StepForward("COMMAND_STEP_FORWARD")
}