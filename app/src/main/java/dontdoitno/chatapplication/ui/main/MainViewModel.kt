package dontdoitno.chatapplication.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dontdoitno.chatapplication.data.api.NetworkService
import dontdoitno.chatapplication.data.api.WebSocketManager
import dontdoitno.chatapplication.data.local.CredentialsStore
import dontdoitno.chatapplication.data.model.ApiResult
import dontdoitno.chatapplication.data.model.Message
import dontdoitno.chatapplication.data.model.MessageData
import dontdoitno.chatapplication.data.model.TextData
import dontdoitno.chatapplication.data.model.WebSocketEvent
import dontdoitno.chatapplication.data.repository.ChatRepository
import dontdoitno.chatapplication.util.SingleLiveEvent
import kotlinx.coroutines.launch

sealed class NavigationEvent {
    data class OpenMessages(val channel: String) : NavigationEvent()
    data class OpenImage(val path: String) : NavigationEvent()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val credentialsStore = CredentialsStore(application)

    var username: String = credentialsStore.username ?: ""

    private val _channels = MutableLiveData<List<String>>(emptyList())
    val channels: LiveData<List<String>> = _channels

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _hasMoreMessages = MutableLiveData(true)
    val hasMoreMessages: LiveData<Boolean> = _hasMoreMessages

    private val _selectedChannel = MutableLiveData<String?>(null)
    val selectedChannel: LiveData<String?> = _selectedChannel

    private val _currentImagePath = MutableLiveData<String?>(null)
    val currentImagePath: LiveData<String?> = _currentImagePath

    val navigationEvent = SingleLiveEvent<NavigationEvent>()
    val reloginEvent = SingleLiveEvent<Unit>()
    val errorEvent = SingleLiveEvent<String>()

    private var oldestLoadedId: String = Int.MAX_VALUE.toString()
    private var isLoadingMessages = false

    private val webSocketManager = WebSocketManager(
        scope = viewModelScope,
        onMessage = { event -> handleWebSocketEvent(event) },
        onReloginRequired = { reloginEvent.postValue(Unit) }
    )

    init {
        val token = credentialsStore.token
        if (!token.isNullOrEmpty() && username.isNotEmpty()) {
            NetworkService.updateToken(token)
            webSocketManager.connect(username, token)
            loadChannels()
        }
    }

    fun loadChannels() {
        viewModelScope.launch {
            when (val result = repository.getChannels()) {
                is ApiResult.Success -> _channels.value = result.data
                is ApiResult.Error -> errorEvent.value = result.message
                is ApiResult.Unauthorized -> reloginEvent.call()
            }
        }
    }

    fun selectChannel(channel: String) {
        _selectedChannel.value = channel
        _messages.value = emptyList()
        oldestLoadedId = Int.MAX_VALUE.toString()
        _hasMoreMessages.value = true
        navigationEvent.value = NavigationEvent.OpenMessages(channel)
        loadInitialMessages(channel)
    }

    private fun loadInitialMessages(channel: String) {
        viewModelScope.launch {
            when (val result = repository.getMessages(channel, 20, Int.MAX_VALUE.toString(), true)) {
                is ApiResult.Success -> {
                    val serverList = result.data
                    if (serverList.isNotEmpty()) {
                        oldestLoadedId = serverList.last().id ?: oldestLoadedId
                    }
                    _hasMoreMessages.value = serverList.size >= 20
                    _messages.value = serverList.reversed()
                }
                is ApiResult.Error -> errorEvent.value = result.message
                is ApiResult.Unauthorized -> reloginEvent.call()
            }
        }
    }

    fun loadMoreMessages() {
        val channel = _selectedChannel.value ?: return
        if (isLoadingMessages || _isLoadingMore.value == true) return
        if (_hasMoreMessages.value == false) return

        isLoadingMessages = true
        _isLoadingMore.value = true

        viewModelScope.launch {
            when (val result = repository.getMessages(channel, 20, oldestLoadedId, true)) {
                is ApiResult.Success -> {
                    val serverList = result.data
                    if (serverList.isNotEmpty()) {
                        oldestLoadedId = serverList.last().id ?: oldestLoadedId
                    }
                    _hasMoreMessages.value = serverList.size >= 20
                    val currentMessages = _messages.value ?: emptyList()
                    _messages.value = serverList.reversed() + currentMessages
                }
                is ApiResult.Error -> errorEvent.value = result.message
                is ApiResult.Unauthorized -> reloginEvent.call()
            }
            _isLoadingMore.value = false
            isLoadingMessages = false
        }
    }

    fun sendMessage(text: String) {
        val channel = _selectedChannel.value ?: return
        if (text.isBlank()) return

        val message = Message(
            from = username,
            to = channel,
            data = MessageData(text = TextData(text))
        )

        viewModelScope.launch {
            when (val result = repository.sendMessage(message)) {
                is ApiResult.Success -> {
                    // Message will arrive via WebSocket
                }
                is ApiResult.Error -> errorEvent.value = result.message
                is ApiResult.Unauthorized -> reloginEvent.call()
            }
        }
    }

    fun openImage(path: String) {
        _currentImagePath.value = path
        navigationEvent.value = NavigationEvent.OpenImage(path)
    }

    fun closeImage() {
        _currentImagePath.value = null
    }

    fun deselectChannel() {
        _selectedChannel.value = null
        _messages.value = emptyList()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            webSocketManager.disconnect()
            NetworkService.updateToken(null)
            credentialsStore.clear()
            reloginEvent.call()
        }
    }

    private fun handleWebSocketEvent(event: WebSocketEvent) {
        event.newMessage?.let { newMessageEvent ->
            val msg = newMessageEvent.msg
            val currentChannel = _selectedChannel.value
            if (msg.to == currentChannel || msg.from == currentChannel) {
                val currentMessages = _messages.value ?: emptyList()
                val alreadyExists = currentMessages.any { it.id == msg.id }
                if (!alreadyExists) {
                    _messages.postValue(currentMessages + msg)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}
