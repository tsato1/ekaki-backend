package com.tsato.data

import com.tsato.data.models.Ping
import com.tsato.gson
import com.tsato.server
import com.tsato.util.Constants.PING_FREQUENCY
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Player(
    val userName: String,
    var socket: WebSocketSession,
    val clientId: String,
    var isDrawing: Boolean = false,
    var score: Int = 0,
    var rank: Int = 0
) {
    private var pingJob: Job? = null
    private var pingTime = 0L
    private var pongTime = 0L
    var isOnline = true

    fun startPing() {
        pingJob?.cancel()
        pingJob = GlobalScope.launch {
            while (true) {
                sendPing()
                delay(PING_FREQUENCY)
            }
        }
    }

    private suspend fun sendPing() {
        pingTime = System.currentTimeMillis()
        socket.send(Frame.Text(gson.toJson(Ping())))
        delay(PING_FREQUENCY)
        if (pingTime - pongTime > PING_FREQUENCY) {
            isOnline = false
            server.playerLeft(clientId)
            pingJob?.cancel()
        }
    }

    fun receivedPongTime() {
        pongTime = System.currentTimeMillis()
        isOnline = true
    }

    fun disconnect() {
        pingJob?.cancel()
    }
}