package com.sushil.chatapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.components.R
import com.sushil.chatapp.R.*
import com.sushil.chatapp.databinding.UserChatRowLayBinding
import com.sushil.chatapp.models.Friend
import com.sushil.chatapp.utils.ImageUtil

class FriendListAdapter( private val onItemClick: (String) -> Unit) : ListAdapter<Friend, FriendListAdapter.FriendViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Friend>() {
            override fun areItemsTheSame(oldItem: Friend, newItem: Friend) = oldItem.number == newItem.number
            override fun areContentsTheSame(oldItem: Friend, newItem: Friend) = oldItem == newItem
        }
    }

    inner class FriendViewHolder(private val binding: UserChatRowLayBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            binding.txtName.text = friend.name
            ImageUtil.loadBase64IntoImageView(friend.profileImageUrl, binding.imgUser)
           // Glide.with(binding.avatar).load(friend.avatar).into(binding.avatar)
            if(!friend.onlineStatus){
                binding.onlineIndicator.visibility = View.GONE
                binding.txtStatus.text= friend.lastMsg
                binding.txtStatus.setTextColor(Color.parseColor("#636363"))
            }else{
                binding.onlineIndicator.visibility = View.VISIBLE
                if(friend.isTyping){
                    binding.txtStatus.text= "Typing..."
                    binding.txtStatus.setTextColor(Color.parseColor("#F28C28"))
                }else{
                    binding.txtStatus.text= friend.lastMsg
                    binding.txtStatus.setTextColor(Color.parseColor("#636363"))
                }
            }

            if(friend.unreadCount>0){
                binding.txtUnreadCount.text = friend.unreadCount.toString()
                binding.layUnreadCount.visibility = View.VISIBLE
                binding.rowLay.setBackgroundColor(ContextCompat.getColor(binding.rowLay.context, color.light_white))
            }else{
                binding.layUnreadCount.visibility = View.GONE
                binding.rowLay.setBackgroundColor(ContextCompat.getColor(binding.rowLay.context, color.white))
            }

            binding.root.setOnClickListener({
                onItemClick(friend.number)
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = UserChatRowLayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: List<Friend>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

}