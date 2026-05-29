package dontdoitno.chatapplication.ui.main

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dontdoitno.chatapplication.data.api.NetworkService
import dontdoitno.chatapplication.data.api.WebSocketManager
import dontdoitno.chatapplication.data.local.CredentialsStore
import dontdoitno.chatapplication.data.local.LocalDataStore
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
    private val localDataStore = LocalDataStore(application)

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

    private val _isOnline = MutableLiveData(false)
    val isOnline: LiveData<Boolean> = _isOnline

    val navigationEvent = SingleLiveEvent<NavigationEvent>()
    val reloginEvent = SingleLiveEvent<Unit?>()
    val errorEvent = SingleLiveEvent<String>()

    private var oldestLoadedId: String = Int.MAX_VALUE.toString()
    private var isLoadingMessages = false

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.postValue(true)
            onNetworkAvailable()
        }

        override fun onLost(network: Network) {
            _isOnline.postValue(false)
        }
    }

    private val webSocketManager = WebSocketManager(
        scope = viewModelScope,
        onMessage = { event -> handleWebSocketEvent(event) },
        onReloginRequired = { reloginEvent.postValue(null) }
    )

    init {
        val token = credentialsStore.token
        if (!token.isNullOrEmpty() && username.isNotEmpty()) {
            NetworkService.updateToken(token)

            // Show cached data immediately
            val cachedChannels = localDataStore.loadChannels()
            if (cachedChannels.isNotEmpty()) {
                _channels.value = cachedChannels
            }

            registerNetworkCallback()

            if (isCurrentlyOnline()) {
                _isOnline.value = true
                webSocketManager.connect(username, token)
                loadChannels()
            }
        }
    }

    private fun isCurrentlyOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try {
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {}
    }

    private fun onNetworkAvailable() {
        val token = credentialsStore.token ?: return
        viewModelScope.launch {
            webSocketManager.connect(username, token)
            loadChannelsInternal()
            val channel = _selectedChannel.value
            if (channel != null) {
                reloadCurrentChannel(channel)
            }
            sendPendingMessages()
        }
    }

    fun loadChannels() {
        viewModelScope.launch { loadChannelsInternal() }
    }

    private suspend fun loadChannelsInternal() {
        when (val result = repository.getChannels()) {
            is ApiResult.Success -> {
                _channels.value = result.data
                localDataStore.saveChannels(result.data)
            }
            is ApiResult.Error -> errorEvent.postValue(result.message)
            is ApiResult.Unauthorized -> reloginEvent.call()
        }
    }

    fun selectChannel(channel: String) {
        _selectedChannel.value = channel
        navigationEvent.value = NavigationEvent.OpenMessages(channel)

        // Show cache immediately
        val cached = localDataStore.loadMessages(channel)
        _messages.value = cached
        oldestLoadedId = Int.MAX_VALUE.toString()
        _hasMoreMessages.value = true

        if (isCurrentlyOnline()) {
            loadInitialMessages(channel)
        }
    }

    private fun reloadCurrentChannel(channel: String) {
        _messages.value = emptyList()
        oldestLoadedId = Int.MAX_VALUE.toString()
        _hasMoreMessages.value = true
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
                    val merged = dedup(serverList.reversed())
                    _messages.value = merged
                    localDataStore.saveMessages(channel, merged)
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
                    val current = _messages.value ?: emptyList()
                    val merged = dedup(serverList.reversed() + current)
                    _messages.value = merged
                    localDataStore.saveMessages(channel, merged)
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

        val localId = "local_${System.currentTimeMillis()}"
        val message = Message(
            id = localId,
            from = username,
            to = channel,
            data = MessageData(text = TextData(text))
        )

        if (!isCurrentlyOnline()) {
            localDataStore.addPendingMessage(message)
            errorEvent.value = getApplication<Application>()
                .getString(dontdoitno.chatapplication.R.string.error_offline_queued)
            return
        }

        viewModelScope.launch { sendMessageOnline(message) }
    }

    private suspend fun sendMessageOnline(message: Message) {
        when (val result = repository.sendMessage(message.copy(id = null))) {
            is ApiResult.Success -> {
                message.id?.let { localDataStore.removePendingMessage(it) }
                _selectedChannel.value?.let { fetchMessages(it) }
            }
            is ApiResult.Error -> errorEvent.postValue(result.message)
            is ApiResult.Unauthorized -> reloginEvent.call()
        }
    }

    private suspend fun fetchMessages(channel: String) {
        when (val result = repository.getMessages(channel, 20, Int.MAX_VALUE.toString(), true)) {
            is ApiResult.Success -> {
                val serverList = result.data
                if (serverList.isNotEmpty()) {
                    oldestLoadedId = serverList.last().id ?: oldestLoadedId
                }
                _hasMoreMessages.postValue(serverList.size >= 20)
                val merged = dedup(serverList.reversed())
                _messages.postValue(merged)
                localDataStore.saveMessages(channel, merged)
            }
            is ApiResult.Error -> errorEvent.postValue(result.message)
            is ApiResult.Unauthorized -> reloginEvent.call()
        }
    }

    private fun sendPendingMessages() {
        viewModelScope.launch {
            val pending = localDataStore.getPendingMessages()
            for (message in pending) {
                when (val result = repository.sendMessage(message)) {
                    is ApiResult.Success -> message.id?.let { localDataStore.removePendingMessage(it) }
                    is ApiResult.Unauthorized -> { reloginEvent.call(); return@launch }
                    is ApiResult.Error -> return@launch
                }
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
            if (msg.to == currentChannel || msg.from == username && msg.to == currentChannel) {
                val current = _messages.value ?: emptyList()
                if (current.none { it.id == msg.id }) {
                    val updated = current + msg
                    _messages.postValue(updated)
                    localDataStore.saveMessages(currentChannel ?: return@let, updated)
                }
            }
        }
    }

    private fun dedup(messages: List<Message>): List<Message> =
        messages.distinctBy { it.id ?: System.nanoTime().toString() }

    override fun onCleared() {
        super.onCleared()
        try { connectivityManager.unregisterNetworkCallback(networkCallback) } catch (_: Exception) {}
        webSocketManager.disconnect()
    }
}
