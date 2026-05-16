package dontdoitno.chatapplication.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dontdoitno.chatapplication.R
import dontdoitno.chatapplication.data.api.NetworkService
import dontdoitno.chatapplication.data.local.CredentialsStore
import dontdoitno.chatapplication.data.model.ApiResult
import dontdoitno.chatapplication.data.repository.ChatRepository
import dontdoitno.chatapplication.util.SingleLiveEvent
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val credentialsStore = CredentialsStore(application)

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val loginError = SingleLiveEvent<String>()
    val loginSuccessEvent = SingleLiveEvent<Unit>()
    val skipLoginEvent = SingleLiveEvent<Unit>()

    fun checkSavedCredentials() {
        if (credentialsStore.hasCredentials()) {
            val savedToken = credentialsStore.token
            if (!savedToken.isNullOrEmpty()) {
                NetworkService.updateToken(savedToken)
                skipLoginEvent.call()
            } else {
                // Try to re-login with saved credentials
                val username = credentialsStore.username ?: return
                val password = credentialsStore.password ?: return
                viewModelScope.launch {
                    doLogin(username, password)
                }
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            loginError.value = getApplication<Application>().getString(R.string.error_empty_credentials)
            return
        }
        viewModelScope.launch {
            doLogin(username, password)
        }
    }

    private suspend fun doLogin(username: String, password: String) {
        _isLoading.value = true
        when (val result = repository.login(username, password)) {
            is ApiResult.Success -> {
                val token = result.data
                credentialsStore.username = username
                credentialsStore.password = password
                credentialsStore.token = token
                NetworkService.updateToken(token)
                loginSuccessEvent.call()
            }
            is ApiResult.Error -> {
                loginError.value = result.message
            }
            is ApiResult.Unauthorized -> {
                loginError.value = getApplication<Application>().getString(R.string.error_login_failed)
            }
        }
        _isLoading.value = false
    }
}
