package dontdoitno.chatapplication.data.repository

import dontdoitno.chatapplication.data.api.NetworkService
import dontdoitno.chatapplication.data.model.ApiResult
import dontdoitno.chatapplication.data.model.LoginRequest
import dontdoitno.chatapplication.data.model.Message

class ChatRepository {

    private val api = NetworkService.chatApi

    suspend fun login(username: String, password: String): ApiResult<String> {
        return try {
            val response = api.login(LoginRequest(username, password))
            when {
                response.isSuccessful -> {
                    val token = response.body()?.string() ?: ""
                    ApiResult.Success(token)
                }
                response.code() == 401 -> ApiResult.Unauthorized
                else -> ApiResult.Error("Login failed: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getChannels(): ApiResult<List<String>> {
        return try {
            val response = api.getChannels()
            when {
                response.isSuccessful -> ApiResult.Success(response.body() ?: emptyList())
                response.code() == 401 -> ApiResult.Unauthorized
                else -> ApiResult.Error("Failed to load channels: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMessages(
        channel: String,
        limit: Int = 20,
        lastKnownId: String = Int.MAX_VALUE.toString(),
        reverse: Boolean = true
    ): ApiResult<List<Message>> {
        return try {
            val response = api.getMessages(channel, limit, lastKnownId, reverse)
            when {
                response.isSuccessful -> ApiResult.Success(response.body() ?: emptyList())
                response.code() == 401 -> ApiResult.Unauthorized
                else -> ApiResult.Error("Failed to load messages: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun sendMessage(message: Message): ApiResult<Unit> {
        return try {
            val response = api.sendMessage(message)
            when {
                response.isSuccessful -> ApiResult.Success(Unit)
                response.code() == 401 -> ApiResult.Unauthorized
                else -> ApiResult.Error("Failed to send message: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun logout(): ApiResult<Unit> {
        return try {
            val response = api.logout()
            when {
                response.isSuccessful -> ApiResult.Success(Unit)
                response.code() == 401 -> ApiResult.Unauthorized
                else -> ApiResult.Error("Logout failed: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}
