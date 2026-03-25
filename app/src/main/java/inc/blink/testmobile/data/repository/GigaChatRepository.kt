package inc.blink.testmobile.data.repository

import inc.blink.testmobile.data.model.ChatMessage
import inc.blink.testmobile.data.remote.ChatRequest
import inc.blink.testmobile.data.remote.Message
import inc.blink.testmobile.data.remote.RetrofitClient
import java.util.UUID

class GigaChatRepository {
    private val authApi = RetrofitClient.authApi
    private val chatApi = RetrofitClient.chatApi
    private var cachedToken: String? = null
    private var tokenExpiresAt: Long = 0



    //строка авторизации GigaChat
    private val authorizationKey = "MDE5ZDI2MDgtMDMwNS03ODM5LWI2ODktODViZDk2OTM1ZGZmOmRlYzE0Yjk0LTM5NGEtNGI2Zi04ZjYzLWRlMmE5MGU3ZTA3OA=="

    suspend fun fetchAiResponse(userMessage: String, history: List<ChatMessage>): String {
        val token = getValidToken()

        val apiMessages = history.map {
            Message(
                role = if (it.isUser) "user" else "assistant",
                content = it.text
            )
        }.toMutableList()

        apiMessages.add(Message(role = "user", content = userMessage))
        val request = ChatRequest(messages = apiMessages)
        val response = chatApi.getChatCompletion(
            token = "Bearer $token",
            rqUid = UUID.randomUUID().toString(),
            request = request
        )

        return response.choices.firstOrNull()?.message?.content
            ?: throw Exception("Пустой ответ от сервера")
    }

    private suspend fun getValidToken(): String {
        val currentTime = System.currentTimeMillis()
        if (cachedToken != null && currentTime < tokenExpiresAt) {
            return cachedToken!!
        }
        // Запрос нового токена с уникальным RqUID
        val response = authApi.getAccessToken(
            auth = "Basic $authorizationKey",
            rqUid = UUID.randomUUID().toString(),
            scope = "GIGACHAT_API_PERS"
        )
        cachedToken = response.accessToken
        tokenExpiresAt = response.expiresAt
        return cachedToken!!
    }
}
