package com.sushil.chatapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sushil.chatapp.R
import com.sushil.chatapp.databinding.UserChatRowLayBinding
import com.sushil.chatapp.models.User
import com.sushil.chatapp.utils.ImageUtil

class ChatListAdapter( private val onItemClick: (String) -> Unit) : ListAdapter<User, ChatListAdapter.FriendViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.number == newItem.number
            override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
        }
    }

    inner class FriendViewHolder(private val binding: UserChatRowLayBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: User) {
            binding.txtName.text = friend.name
            ImageUtil.loadBase64IntoImageView(friend.profileImageUrl, binding.imgUser)
           // Glide.with(binding.avatar).load(friend.avatar).into(binding.avatar)
            if(friend.onlineStatus){
                binding.txtStatus.text= "Online"
                binding.txtStatus.setTextColor(Color.parseColor("#4CBB17"))
            }else{
                binding.txtStatus.text= "Offline"
                binding.txtStatus.setTextColor(Color.parseColor("#636363"))
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

    override fun submitList(list: List<User>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

}