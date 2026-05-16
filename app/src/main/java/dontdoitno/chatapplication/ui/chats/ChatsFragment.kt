package dontdoitno.chatapplication.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dontdoitno.chatapplication.databinding.FragmentChatsBinding
import dontdoitno.chatapplication.ui.main.MainViewModel

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var channelAdapter: ChannelAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        channelAdapter = ChannelAdapter { channel ->
            viewModel.selectChannel(channel)
        }
        binding.rvChannels.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = channelAdapter
        }
    }

    private fun setupObservers() {
        viewModel.channels.observe(viewLifecycleOwner) { channels ->
            channelAdapter.submitList(channels)
        }
        viewModel.selectedChannel.observe(viewLifecycleOwner) { channel ->
            channelAdapter.setSelectedChannel(channel)
        }
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
