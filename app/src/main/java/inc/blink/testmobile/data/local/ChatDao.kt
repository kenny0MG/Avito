package inc.blink.testmobile.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import inc.blink.testmobile.data.model.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    fun getAllChats(): Flow<List<Chat>>
    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    fun getChatsPaging(): PagingSource<Int, Chat>
    @Query("SELECT * FROM chats WHERE title LIKE :query ORDER BY timestamp DESC")
    fun searchChats(query: String): Flow<List<Chat>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat): Long
    @Update
    suspend fun updateChat(chat: Chat)
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: Long)
}
