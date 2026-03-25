package inc.blink.testmobile.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class ChatMessage(@PrimaryKey(autoGenerate = true) val id: Long = 0, val chatId: Long, val text: String, val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val imageUrl: String? = null
)
