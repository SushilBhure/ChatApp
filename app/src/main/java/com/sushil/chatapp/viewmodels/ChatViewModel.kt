package com.sushil.chatapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sushil.chatapp.models.User
import com.sushil.chatapp.repository.ChatRepository
import com.sushil.chatapp.repository.UserRepository

class ChatViewModel: ViewModel() {

    private val repository = ChatRepository()

    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>> = _friends

    fun observeFriends(ID: String) {
        repository.getFriends({friendList ->
            _friends.postValue(friendList)
        }, currentUserId = ID)
    }
}