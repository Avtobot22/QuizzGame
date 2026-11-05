package com.quizzgame.core.session.model

fun interface DeviceIdProvider {

    fun get(): String
}
