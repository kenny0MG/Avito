package inc.blink.testmobile.ui.chatlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import inc.blink.testmobile.data.local.AppDatabase
import inc.blink.testmobile.data.model.Chat
import inc.blink.testmobile.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ChatRepository
    val searchQuery = MutableStateFlow("")

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChatRepository(database.chatDao(), database.messageDao())
    }

    val chatPagingFlow: Flow<PagingData<Chat>> = repository.getChatsPaging()
        .cachedIn(viewModelScope)
    val searchResults: StateFlow<List<Chat>> = searchQuery
        .flatMapLatest { query ->
            repository.searchChats(query)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())




    fun createChat(title: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createChat(title)
            onCreated(id)
        }
    }
}
