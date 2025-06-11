package com.sushil.chatapp.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sushil.chatapp.models.User
import com.sushil.chatapp.utils.PrefManager

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val messagesDB = db.collection("messages")
    private val usersDB = db.collection("users")

    fun getFriends(callback: (List<User>) -> Unit, currentUserId: String) {
        usersDB
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull {
                          //  it.toObject(User::class.java)
                            it.toObject(User::class.java)?.takeIf { it.number != currentUserId }

                    }
                    callback(list)
                }
            }
    }

   /* suspend fun sendMessage(chatId: String, message: ChatMessage) {
        messages.document(chatId)
            .collection("chat")
            .add(message)
            .await()
    }

    fun getMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = messages.document(chatId)
            .collection("chat")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messageList = snapshot?.documents?.mapNotNull { it.toObject(ChatMessage::class.java) }
                trySend(messageList ?: emptyList())
            }

        awaitClose { listener.remove() }
    }*/
}