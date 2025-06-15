package com.sushil.chatapp.models

data class Friend(val name: String="",
                  val number: String="",
                  val profileImageUrl: String="",
                  val onlineStatus: Boolean=false,
                  val isTyping: Boolean=false,
                  val unreadCount: Int = 0,
                  val lastMsg: String= "",
                  val lastMsgTimestamp: Long = 0L)
