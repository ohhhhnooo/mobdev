package dontdoitno.chatapplication.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkService {

    private const val BASE_URL = "https://faerytea.name/"

    @Volatile
    var currentToken: String? = null

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder().apply {
            currentToken?.let { token ->
                addHeader("X-Auth-Token", token)
            }
        }.build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val chatApi: ChatApi = retrofit.create(ChatApi::class.java)

    fun updateToken(token: String?) {
        currentToken = token
    }

    fun getBaseUrl(): String = BASE_URL

    fun getOkHttpClient(): OkHttpClient = okHttpClient
}
