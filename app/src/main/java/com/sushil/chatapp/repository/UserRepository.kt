package com.sushil.chatapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sushil.chatapp.models.User
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
}