package inc.blink.testmobile.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val lastMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
