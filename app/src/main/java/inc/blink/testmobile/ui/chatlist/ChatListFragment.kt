package inc.blink.testmobile.ui.chatlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import inc.blink.testmobile.R
import inc.blink.testmobile.databinding.FragmentChatListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatListFragment : Fragment() {
    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatListViewModel by viewModels()
    private lateinit var pagingAdapter: ChatListPagingAdapter
    private lateinit var searchAdapter: ChatListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupSearch()
        setupFab()
        observeData()
    }

    private fun setupRecyclerViews() {
        pagingAdapter = ChatListPagingAdapter { chat -> navigateToChat(chat.id) }
        searchAdapter = ChatListAdapter { chat -> navigateToChat(chat.id) }
        
        binding.rvChats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChats.adapter = pagingAdapter
    }


    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.chatPagingFlow.collectLatest { pagingData ->
                        if (_binding != null && viewModel.searchQuery.value.isEmpty()) {
                            binding.rvChats.adapter = pagingAdapter
                            pagingAdapter.submitData(pagingData)
                        }
                    }
                }


                launch {
                    viewModel.searchResults.collectLatest { results ->
                        if (_binding != null && viewModel.searchQuery.value.isNotEmpty()) {
                            binding.rvChats.adapter = searchAdapter
                            searchAdapter.submitList(results)
                            binding.tvEmpty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
                        } else if (_binding != null) {
                            binding.tvEmpty.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text?.toString() ?: ""
            viewModel.searchQuery.value = query
            if (query.isEmpty()) {
                if (_binding != null) {
                    binding.rvChats.adapter = pagingAdapter
                }
            }
        }
    }
    private fun setupFab() {
        binding.fabNewChat.setOnClickListener {
            viewModel.createChat("Новый чат") { id ->
                navigateToChat(id)
            }
        }
    }
    private fun navigateToChat(chatId: Long) {
        val action = ChatListFragmentDirections.actionChatListFragmentToChatFragment(chatId)
        findNavController().navigate(action)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
