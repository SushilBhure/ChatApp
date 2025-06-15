package com.sushil.chatapp.models

import com.google.firebase.firestore.PropertyName

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val haveSeen: Boolean = false
)
