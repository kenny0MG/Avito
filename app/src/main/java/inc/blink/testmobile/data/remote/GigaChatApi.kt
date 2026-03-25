package inc.blink.testmobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface GigaChatApi {
    @FormUrlEncoded
    @POST("api/v2/oauth")
    suspend fun getAccessToken(
        @Header("Authorization") auth: String,
        @Header("RqUID") rqUid: String,
        @Field("scope") scope: String = "GIGACHAT_API_PERS"
    ): TokenResponse
    @POST("api/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") token: String,
        @Header("RqUID") rqUid: String,
        @Body request: ChatRequest
    ): ChatResponse
}

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_at") val expiresAt: Long
)
data class ChatRequest(
    val model: String = "GigaChat",
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    val n: Int = 1,
    val stream: Boolean = false
)
data class Message(
    val role: String,
    val content: String
)
data class ChatResponse(
    val choices: List<Choice>,
    val created: Long,
    val model: String
)
data class Choice(
    val message: Message,
    val index: Int,
    @SerializedName("finish_reason") val finishReason: String
)
