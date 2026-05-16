package dontdoitno.chatapplication.ui.chats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dontdoitno.chatapplication.databinding.ItemChannelBinding

class ChannelAdapter(
    private val onChannelClick: (String) -> Unit
) : ListAdapter<String, ChannelAdapter.ChannelViewHolder>(DIFF_CALLBACK) {

    private var selectedChannel: String? = null

    fun setSelectedChannel(channel: String?) {
        val oldSelected = selectedChannel
        selectedChannel = channel
        if (oldSelected != channel) {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(getItem(position), getItem(position) == selectedChannel)
    }

    inner class ChannelViewHolder(private val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: String, isSelected: Boolean) {
            binding.tvChannelName.text = channel
            binding.tvChannelName.setTypeface(
                null,
                if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL
            )
            val bgColor = if (isSelected) {
                com.google.android.material.color.MaterialColors.getColor(
                    binding.root,
                    com.google.android.material.R.attr.colorSecondaryContainer
                )
            } else {
                android.graphics.Color.TRANSPARENT
            }
            binding.root.setBackgroundColor(bgColor)
            binding.root.setOnClickListener { onChannelClick(channel) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    }
}
