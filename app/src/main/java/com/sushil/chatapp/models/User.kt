package com.sushil.chatapp.models

import android.util.Log
import com.google.firebase.Timestamp

data class User(val name: String="",
                val number: String="",
                val profileImageUrl: String="",
                val onlineStatus: Boolean=false,
                val typingFor: String="",
                val unreadCount: Int = 0,
                val lastMsg: String= "",
                val lastMsgTimestamp: Long = 0L)
