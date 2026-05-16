package dontdoitno.chatapplication.data.api

import dontdoitno.chatapplication.data.model.LoginRequest
import dontdoitno.chatapplication.data.model.Message
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ChatApi {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>

    @GET("channels")
    suspend fun getChannels(): Response<List<String>>

    @GET("channel/{name}")
    suspend fun getMessages(
        @Path("name") channel: String,
        @Query("limit") limit: Int,
        @Query("lastKnownId") lastKnownId: String,
        @Query("reverse") reverse: Boolean
    ): Response<List<Message>>

    @POST("messages")
    suspend fun sendMessage(@Body message: Message): Response<ResponseBody>

    @POST("logout")
    suspend fun logout(): Response<ResponseBody>
}
