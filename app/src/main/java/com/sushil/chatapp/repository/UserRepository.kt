package com.sushil.chatapp.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.sushil.chatapp.models.Friend
import com.sushil.chatapp.models.User
import com.sushil.chatapp.utils.ChatUtils
import kotlinx.coroutines.tasks.await


class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    suspend fun createUser(phone: String, user: User) {
        users.document(phone)
            .set(user)
            .await()
    }


    suspend fun getUser(phone: String): User? {
        val doc = users.document(phone).get().await()
        return if (doc.exists()) doc.toObject(User::class.java) else null
    }

    @SuppressLint("SuspiciousIndentation")
    fun getUserLiveData(phone: String): LiveData<User?> {
        val liveData = MutableLiveData<User?>()

           users
            .document(phone)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    liveData.postValue(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // Skip the cached snapshot, only allow server data
                    if (!snapshot.metadata.isFromCache) {
                        val user = snapshot.toObject(User::class.java)
                        liveData.postValue(user)
                    }
                }
            }

        return liveData
    }



    suspend fun isUserRegistered(phoneNumber: String): Boolean {
        val documentSnapshot = db.collection("users")
            .document(phoneNumber)
            .get()
            .await()
        return documentSnapshot.exists()
    }

    /*suspend fun getUsers(): List<String> {
        val snapshot = db.collection("users").get().await()
        return snapshot.documents
    }*/

    fun updateUserName(currentUserID: String, name: String) {
        users
            .document(currentUserID)
            .update("name", name)
            .addOnSuccessListener {
                Log.d("UserRepository", "Name updated to $name")
            }
            .addOnFailureListener {
                Log.e("UserRepository", "Name update failed: ${it.message}")
            }
    }

    fun updateProfileImage(currentUserID: String, imgUrl: String) {
        users
            .document(currentUserID)
            .update("profileImageUrl", imgUrl)
            .addOnSuccessListener {
                Log.d("UserRepository", "ProfileImage updated")
            }
            .addOnFailureListener {
                Log.e("UserRepository", "ProfileImage update failed: ${it.message}")
            }
    }

    suspend fun updateUserStatus(currentUserID: String, status: Boolean) {
        users
            .document(currentUserID)
            .update("onlineStatus", status)
            .addOnSuccessListener {
                Log.d("UserRepository", "Status updated to $status")
            }
            .addOnFailureListener {
                Log.e("UserRepository", "Status update failed: ${it.message}")
            }
    }

    suspend fun updateTypingStatus(phone: String, typingFor: String) {
        users.document(phone).update("typingFor", typingFor).await()
    }

    private val listeners = mutableListOf<ListenerRegistration>()

    fun getFriendListLiveData(currentUid: String): LiveData<List<Friend>> {
        val liveData = MutableLiveData<List<Friend>>()
        val tempFriendsMap = mutableMapOf<String, Friend>()

        // Step 1: Fetch all users
        val userListListener = db.collection("users")
            .addSnapshotListener { snapshots, _ ->
                snapshots?.documents?.forEach { doc ->
                    val friendUid = doc.id
                    if (friendUid == currentUid) return@forEach

                    // Avoid duplicate listeners
                    if (tempFriendsMap.containsKey(friendUid)) return@forEach

                    val userRef = db.collection("users").document(friendUid)
                    val chatId = ChatUtils.generateChatId(currentUid, friendUid)
                    val chatRef = db.collection("chats").document(chatId)

                    var userSnapshot: DocumentSnapshot? = null
                    var chatSnapshot: DocumentSnapshot? = null
                    var chatExists = false

                    val updateFriend = {
                        if (userSnapshot != null) {
                            val friend = Friend(
                               // uid = friendUid,
                                name = userSnapshot?.getString("name") ?: "",
                                number = userSnapshot?.getString("number") ?: "",
                                profileImageUrl = userSnapshot?.getString("profileImageUrl") ?: "",
                                onlineStatus = userSnapshot?.getBoolean("onlineStatus") ?: false,
                                isTyping = if (chatExists) (chatSnapshot?.getBoolean("typingFor_${currentUid}")?:false) else false,
                                unreadCount = if (chatExists) (chatSnapshot?.getLong("unreadCount_${currentUid}") ?: 0L).toInt() else 0,
                                lastMsg = if (chatExists) chatSnapshot?.getString("lastMessage") ?: "" else "",
                                lastMsgTimestamp = if (chatExists) chatSnapshot?.getLong("lastMessageTimestamp") ?: 0L else 0L
                            )
                            tempFriendsMap[friendUid] = friend
                            liveData.postValue(tempFriendsMap.values.sortedByDescending { it.lastMsgTimestamp })
                        }
                    }

                    val userListener = userRef.addSnapshotListener { snapshot, _ ->
                        userSnapshot = snapshot
                        updateFriend()
                    }

                    val chatListener = chatRef.addSnapshotListener { snapshot, _ ->
                        chatSnapshot = snapshot
                        chatExists = snapshot != null && snapshot.exists()
                        updateFriend()
                    }

                    listeners.add(userListener)
                    listeners.add(chatListener)
                }
            }

        listeners.add(userListListener)

        return liveData
    }

    fun stopAllListeners() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

}