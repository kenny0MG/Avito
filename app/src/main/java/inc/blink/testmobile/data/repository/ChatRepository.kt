package inc.blink.testmobile.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import inc.blink.testmobile.data.local.ChatDao
import inc.blink.testmobile.data.local.MessageDao
import inc.blink.testmobile.data.model.Chat
import inc.blink.testmobile.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) {
    fun getChatsPaging(): Flow<PagingData<Chat>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { chatDao.getChatsPaging() }
        ).flow
    }


    suspend fun createChat(title: String): Long {
        return chatDao.insertChat(Chat(title = title))
    }
    fun getMessagesForChat(chatId: Long): Flow<List<ChatMessage>> {
        return messageDao.getMessagesForChat(chatId)
    }
    suspend fun insertMessage(message: ChatMessage) {
        messageDao.insertMessage(message)
        // Update last message and timestamp in chat
        val chat = Chat(id = message.chatId, title = "Чат ${message.chatId}", lastMessage = message.text, timestamp = message.timestamp)
        chatDao.updateChat(chat)
    }
    fun searchChats(query: String): Flow<List<Chat>> {
        return chatDao.searchChats("%$query%")
    }
}
