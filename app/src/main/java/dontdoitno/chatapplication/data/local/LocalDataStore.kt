package dontdoitno.chatapplication.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dontdoitno.chatapplication.data.model.Message

class LocalDataStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveChannels(channels: List<String>) {
        prefs.edit().putString(KEY_CHANNELS, gson.toJson(channels)).apply()
    }

    fun loadChannels(): List<String> {
        val json = prefs.getString(KEY_CHANNELS, null) ?: return emptyList()
        return gson.fromJson(json, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    }

    fun saveMessages(channel: String, messages: List<Message>) {
        val toSave = if (messages.size > MAX_CACHED_MESSAGES) messages.takeLast(MAX_CACHED_MESSAGES) else messages
        prefs.edit().putString(keyMessages(channel), gson.toJson(toSave)).apply()
    }

    fun loadMessages(channel: String): List<Message> {
        val json = prefs.getString(keyMessages(channel), null) ?: return emptyList()
        return gson.fromJson(json, object : TypeToken<List<Message>>() {}.type) ?: emptyList()
    }

    fun addPendingMessage(message: Message) {
        val pending = getPendingMessages().toMutableList()
        pending.add(message)
        prefs.edit().putString(KEY_PENDING, gson.toJson(pending)).apply()
    }

    fun removePendingMessage(localId: String) {
        val pending = getPendingMessages().filter { it.id != localId }
        prefs.edit().putString(KEY_PENDING, gson.toJson(pending)).apply()
    }

    fun getPendingMessages(): List<Message> {
        val json = prefs.getString(KEY_PENDING, null) ?: return emptyList()
        return gson.fromJson(json, object : TypeToken<List<Message>>() {}.type) ?: emptyList()
    }

    private fun keyMessages(channel: String) = "${KEY_MESSAGES_PREFIX}${channel}"

    companion object {
        private const val PREFS_NAME = "local_data"
        private const val KEY_CHANNELS = "channels"
        private const val KEY_MESSAGES_PREFIX = "messages_"
        private const val KEY_PENDING = "pending_messages"
        private const val MAX_CACHED_MESSAGES = 50
    }
}
