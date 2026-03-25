package inc.blink.testmobile.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import inc.blink.testmobile.data.local.AppDatabase
import inc.blink.testmobile.data.model.ChatMessage
import inc.blink.testmobile.data.repository.ChatRepository
import inc.blink.testmobile.data.repository.GigaChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val dbRepo: ChatRepository
    private val aiRepo = GigaChatRepository()
    private var currentChatId: Long = -1
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    init {
        val db = AppDatabase.getDatabase(application)
        dbRepo = ChatRepository(db.chatDao(), db.messageDao())
    }


    fun setChatId(chatId: Long) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        viewModelScope.launch {
            dbRepo.getMessagesForChat(chatId).collect {
                _messages.value = it
            }
        }
    }
    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            // 1. Save user message to DB
            val userMsg = ChatMessage(chatId = currentChatId, text = text, isUser = true)
            dbRepo.insertMessage(userMsg)

            _isLoading.value = true
            try {
                // 2. Get AI response with context
                val aiResponse = aiRepo.fetchAiResponse(text, _messages.value)
                
                // 3. Save assistant message to DB
                val botMsg = ChatMessage(chatId = currentChatId, text = aiResponse, isUser = false)
                dbRepo.insertMessage(botMsg)
            } catch (e: Exception) {
                // Handle error
                dbRepo.insertMessage(
                    ChatMessage(
                        chatId = currentChatId, 
                        text = "Ошибка: ${e.localizedMessage}", 
                        isUser = false, 
                        isError = true
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
