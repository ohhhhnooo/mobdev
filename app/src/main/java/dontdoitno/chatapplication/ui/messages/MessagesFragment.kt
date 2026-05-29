package dontdoitno.chatapplication.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dontdoitno.chatapplication.databinding.FragmentMessagesBinding
import dontdoitno.chatapplication.ui.main.MainViewModel

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var isLoadingMore = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter(
            username = viewModel.username,
            onImageClick = { path -> viewModel.openImage(path) }
        )

        layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }

        binding.rvMessages.apply {
            this.layoutManager = this@MessagesFragment.layoutManager
            adapter = messagesAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisible = this@MessagesFragment.layoutManager.findFirstVisibleItemPosition()
                    if (firstVisible == 0 && !isLoadingMore) {
                        val hasMore = viewModel.hasMoreMessages.value ?: false
                        if (hasMore) {
                            viewModel.loadMoreMessages()
                        }
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            val wasAtBottom = isAtBottom()
            val previousCount = messagesAdapter.currentList.size
            messagesAdapter.submitList(messages) {
                if (_binding == null) return@submitList
                if (messages.isNotEmpty() && (wasAtBottom || previousCount == 0)) {
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.isLoadingMore.observe(viewLifecycleOwner) { loading ->
            isLoadingMore = loading
            binding.progressLoadMore.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.selectedChannel.observe(viewLifecycleOwner) { channel ->
            binding.tvChannelTitle.text = channel ?: ""
        }

        viewModel.isOnline.observe(viewLifecycleOwner) { online ->
            binding.tvOfflineBanner.visibility = if (online) View.GONE else View.VISIBLE
            binding.btnSend.isEnabled = online
        }
    }

    private fun isAtBottom(): Boolean {
        val lastVisible = this.layoutManager.findLastVisibleItemPosition()
        val itemCount = messagesAdapter.itemCount
        return itemCount == 0 || lastVisible >= itemCount - 1
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.etMessage.setText("")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
