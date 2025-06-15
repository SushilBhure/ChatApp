package com.sushil.chatapp.utils


    object ChatUtils {
        fun generateChatId(user1: String, user2: String): String {
            return listOf(user1, user2).sorted().joinToString("_")
        }
    }
