package dontdoitno.chatapplication.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("name") val name: String,
    @SerializedName("pwd") val pwd: String
)

data class MessageData(
    @SerializedName("Text") val text: TextData? = null,
    @SerializedName("Image") val image: ImageData? = null
)

data class TextData(
    @SerializedName("text") val text: String
)

data class ImageData(
    @SerializedName("link") val link: String
)

data class Message(
    @SerializedName("id") val id: String? = null,
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String? = null,
    @SerializedName("data") val data: MessageData,
    @SerializedName("time") val time: String? = null
)

data class NewMessageEvent(
    @SerializedName("msg") val msg: Message
)

data class TypingChangedEvent(
    @SerializedName("channel") val channel: List<String>
)

data class WebSocketEvent(
    @SerializedName("NewMessage") val newMessage: NewMessageEvent? = null,
    @SerializedName("TypingChanged") val typingChanged: TypingChangedEvent? = null
)

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Unauthorized : ApiResult<Nothing>()
}
