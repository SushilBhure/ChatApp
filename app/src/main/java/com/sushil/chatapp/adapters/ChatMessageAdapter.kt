package com.sushil.chatapp.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sushil.chatapp.R
import com.sushil.chatapp.databinding.ChatItemLayBinding
import com.sushil.chatapp.models.Message
import com.sushil.chatapp.utils.DateTimeUtils

class ChatMessageAdapter(val currentUserID: String) : ListAdapter<Message, ChatMessageAdapter.MessageViewHolder>(

    object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
) {

    inner class MessageViewHolder(private val binding: ChatItemLayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message, dateText: String, showDate: Boolean) {

            if (showDate) {
                binding.txtDate.visibility = View.VISIBLE
                binding.txtDate.text = dateText
            } else {
                binding.txtDate.visibility = View.GONE
            }

            if(message.senderId==currentUserID){
                binding.layMyMsg.visibility = View.VISIBLE
                binding.layFrndMsg.visibility = View.GONE
                binding.txtMyMsg.text = message.message
                binding.txtMyTime.text = DateTimeUtils.formatTimestampToTime(message.timestamp)
                if (message.haveSeen){
                    binding.viewSeen.setBackgroundResource(R.drawable.double_check_seen)
                }else{
                    binding.viewSeen.setBackgroundResource(R.drawable.check_double_unseen)
                }
            }else{
                binding.layMyMsg.visibility = View.GONE
                binding.layFrndMsg.visibility = View.VISIBLE
                binding.txtFrndMsg.text = message.message
                binding.txtFrndTime.text = DateTimeUtils.formatTimestampToTime(message.timestamp)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ChatItemLayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        val currentMessage = getItem(position)
        val currentDate = DateTimeUtils.formatTimestamp(currentMessage.timestamp)

        val showDate = if (position == 0) {
            true // Always show date for the first message
        } else {
            val prevMessage = getItem(position - 1)
            val prevDate = DateTimeUtils.formatTimestamp(prevMessage.timestamp)
            currentDate != prevDate
        }

        holder.bind(currentMessage, currentDate, showDate)
      //  holder.bind(getItem(position))
    }
}