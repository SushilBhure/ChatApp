package com.sushil.chatapp.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushil.chatapp.models.Message
import com.sushil.chatapp.models.User
import com.sushil.chatapp.repository.ChatRepository
import com.sushil.chatapp.repository.UserRepository
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>> = _friends

    fun observeFriends(ID: String) {
        repository.getFriends({friendList ->
            _friends.postValue(friendList)
        }, currentUserId = ID)
    }

    fun observeChat(chatId: String, userId: String) {
        viewModelScope.launch {
            repository.getMessageFlow(chatId, userId).collect { messages ->
                _messages.value = messages
            }
        }
    }

    fun sendMessage(senderID: String, receiverID: String, msg: String) {

        viewModelScope.launch {
            repository.sendMessage(senderID, receiverID, msg)
            }
    }

    private val _typingStatus = MutableLiveData<Boolean>()
    val typingStatus: LiveData<Boolean> get() = _typingStatus

    fun observeTyping(chatId: String, currentUserId: String, lifecycleOwner: LifecycleOwner) {
        repository.observeTypingStatus(chatId, currentUserId)
            .observe(lifecycleOwner) { isTyping ->
                _typingStatus.postValue(isTyping)
            }
    }

    fun setTyping(chatId: String, friendUserID: String, isTyping: Boolean) {
        repository.updateTypingStatus(chatId, friendUserID, isTyping)
    }
}

