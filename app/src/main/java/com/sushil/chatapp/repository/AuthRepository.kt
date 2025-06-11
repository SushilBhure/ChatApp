package com.sushil.chatapp.repository

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.sushil.chatapp.utils.PrefManager
import java.util.concurrent.TimeUnit

class AuthRepository(val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var verificationId: String? = null

    // Directly exposed LiveData
    private val _authState = MutableLiveData<FirebaseUser?>().apply {
        value = auth.currentUser // Initialize with current user
    }

    val authState: LiveData<FirebaseUser?> = _authState

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        _authState.postValue(auth.currentUser) // Will be null if logged out
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun logout() {
        auth.signOut()
    }

    fun checkUserSession(): Boolean {

        return ((auth.currentUser != null) && (PrefManager.getUserId(context)!=null))
    }

    fun startPhoneAuth(
        phone: String,
        activity: Activity,
        callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun setVerificationId(id: String) {
        verificationId = id
    }

    fun getCredential(code: String): PhoneAuthCredential? {
        return verificationId?.let { PhoneAuthProvider.getCredential(it, code) }
    }

    fun signInWithCredential(
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    onError(it.exception?.message ?: "Sign-in failed")
                }
            }
    }

    // Optional cleanup, in case you want to stop listening manually
    fun cleanup() {
        auth.removeAuthStateListener(authListener)
    }
}