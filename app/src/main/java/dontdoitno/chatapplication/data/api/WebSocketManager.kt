package dontdoitno.chatapplication.data.api

import com.google.gson.Gson
import dontdoitno.chatapplication.data.model.WebSocketEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketManager(
    private val scope: CoroutineScope,
    private val onMessage: (WebSocketEvent) -> Unit,
    private val onReloginRequired: () -> Unit
) {
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var username: String = ""
    private var token: String = ""
    private var isConnected = false
    private var shouldReconnect = true

    fun connect(username: String, token: String) {
        this.username = username
        this.token = token
        shouldReconnect = true
        openConnection()
    }

    private fun openConnection() {
        val url = "wss://faerytea.name/ws/$username?token=$token"
        val request = Request.Builder().url(url).build()
        webSocket = NetworkService.getOkHttpClient().newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    isConnected = true
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val event = gson.fromJson(text, WebSocketEvent::class.java)
                        onMessage(event)
                    } catch (e: Exception) {
                        // ignore malformed messages
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    isConnected = false
                    if (response?.code == 401) {
                        onReloginRequired()
                        return
                    }
                    if (shouldReconnect) {
                        scope.launch {
                            delay(3000)
                            if (shouldReconnect) openConnection()
                        }
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    isConnected = false
                    if (code == 1000) {
                        // normal closure
                        return
                    }
                    if (shouldReconnect) {
                        scope.launch {
                            delay(3000)
                            if (shouldReconnect) openConnection()
                        }
                    }
                }
            }
        )
    }

    fun disconnect() {
        shouldReconnect = false
        webSocket?.close(1000, "User logout")
        webSocket = null
        isConnected = false
    }
}
