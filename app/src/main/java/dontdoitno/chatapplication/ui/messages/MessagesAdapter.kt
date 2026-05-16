package dontdoitno.chatapplication.ui.messages

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dontdoitno.chatapplication.R
import dontdoitno.chatapplication.data.model.Message
import dontdoitno.chatapplication.databinding.ItemMessageImageBinding
import dontdoitno.chatapplication.databinding.ItemMessageTextBinding

class MessagesAdapter(
    private val username: String,
    private val onImageClick: (String) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val VIEW_TYPE_TEXT = 0
        private const val VIEW_TYPE_IMAGE = 1
        private const val BASE_URL = "https://faerytea.name/"

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message) =
                oldItem.id != null && oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Message, newItem: Message) =
                oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.data.image != null) VIEW_TYPE_IMAGE else VIEW_TYPE_TEXT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_IMAGE -> {
                val binding = ItemMessageImageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ImageViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageTextBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                TextViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        val isMine = message.from == username
        when (holder) {
            is TextViewHolder -> holder.bind(message, isMine)
            is ImageViewHolder -> holder.bind(message, isMine)
        }
    }

    inner class TextViewHolder(private val binding: ItemMessageTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message, isMine: Boolean) {
            binding.tvSender.text = message.from
            binding.tvMessage.text = message.data.text?.text ?: ""

            if (isMine) {
                binding.senderContainer.gravity = Gravity.END
                binding.tvSender.visibility = View.GONE
                binding.tvMessage.setBackgroundResource(R.drawable.bg_message_mine)
            } else {
                binding.senderContainer.gravity = Gravity.START
                binding.tvSender.visibility = View.VISIBLE
                binding.tvMessage.setBackgroundResource(R.drawable.bg_message_other)
            }
        }
    }

    inner class ImageViewHolder(private val binding: ItemMessageImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message, isMine: Boolean) {
            binding.tvSender.text = message.from
            val link = message.data.image?.link ?: ""
            val thumbUrl = "${BASE_URL}thumb/$link"

            if (isMine) {
                binding.senderContainer.gravity = Gravity.END
                binding.tvSender.visibility = View.GONE
            } else {
                binding.senderContainer.gravity = Gravity.START
                binding.tvSender.visibility = View.VISIBLE
            }

            Glide.with(binding.ivMessage.context)
                .load(thumbUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.ivMessage)

            binding.ivMessage.setOnClickListener {
                onImageClick(link)
            }
        }
    }
}
