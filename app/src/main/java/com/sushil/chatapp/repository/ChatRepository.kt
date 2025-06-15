package com.sushil.chatapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.sushil.chatapp.models.Message
import com.sushil.chatapp.models.User
import com.sushil.chatapp.utils.ChatUtils
import com.sushil.chatapp.utils.PrefManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    val chatDB = db.collection("chatItems")
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

    suspend fun sendMessage(senderUid: String, receiverUid: String, messageText: String) {
        val chatId = ChatUtils.generateChatId(senderUid, receiverUid)
        val message = Message(
            senderId = senderUid,
            receiverId = receiverUid,
            message = messageText,
            timestamp = System.currentTimeMillis(),
            haveSeen = false
        )

        val db = FirebaseFirestore.getInstance()
        val chatRef = db.collection("chats").document(chatId)
        val messageRef = chatRef.collection("messages").document()

        db.runBatch { batch ->
            batch.set(messageRef, message)
            batch.set(chatRef, mapOf(
                "lastMessage" to messageText,
                "typingFor_${receiverUid}" to false,
                "lastMessageTimestamp" to message.timestamp,
                "unreadCount_${receiverUid}" to FieldValue.increment(1)

            ), SetOptions.merge())
        }.addOnSuccessListener {
            Log.d("SendMessage", "Message sent successfully")
        }.addOnFailureListener { e ->
            Log.e("SendMessage", "Failed to send message", e)
        }
    }

     fun getMessageFlow(chatId: String, currentUserId: String): Flow<List<Message>> = callbackFlow {
         val messagesRef = FirebaseFirestore.getInstance()
             .collection("chats")
             .document(chatId)
             .collection("messages")
             .orderBy("timestamp")

         val chatDocRef = db.collection("chats").document(chatId)

        val listener = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                close(error ?: Exception("Snapshot is null"))
                return@addSnapshotListener
            }

            val messages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
            trySend(messages)

            // Auto mark messages as seen
            val unseen = snapshot.documents.filter {
                it.getString("receiverId") == currentUserId && it.getBoolean("haveSeen") == false
            }

            if (unseen.isNotEmpty()) {
                val batch = db.batch()
                unseen.forEach { doc -> batch.update(doc.reference, "haveSeen", true) }

                val unreadField = "unreadCount_$currentUserId"
                batch.update(chatDocRef, unreadField, 0)

                batch.commit()
            }
        }

        awaitClose { listener.remove() }
    }

    suspend fun markMessagesAsSeen(chatId: String, currentUserId: String) {

        val chatRef = chatDB.document(chatId)
        val messagesRef = chatRef.collection("messages")

        messagesRef
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("haveSeen", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()

                // Mark each unseen message as seen
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "haveSeen", true)
                }

                // Reset unread count for this user
                val unreadField = "unreadCount_$currentUserId"
                batch.update(chatRef, unreadField, 0)

                batch.commit()
            }
    }

    fun observeTypingStatus(chatId: String, currentUserID: String): LiveData<Boolean> {
        val typingLiveData = MutableLiveData<Boolean>(false)
        val chatRef =db.collection("chats")
            .document(chatId)

        chatRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val fieldName = "typingFor_$currentUserID"
            val isTyping = snapshot.getBoolean(fieldName) ?: false
            typingLiveData.postValue(isTyping)
        }

        return typingLiveData
    }

    fun updateTypingStatus(chatId: String, friendUserID: String, isTyping: Boolean) {
        val fieldName = "typingFor_$friendUserID"
        val chatRef = db.collection("chats")
            .document(chatId)

        chatRef.update(fieldName, isTyping)
            .addOnFailureListener { e ->
                Log.e("TypingStatus", "Failed to update $fieldName to $isTyping", e)
            }
    }
}