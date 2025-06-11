package com.sushil.chatapp.viewmodels

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.sushil.chatapp.models.User
import com.sushil.chatapp.repository.AuthRepository
import com.sushil.chatapp.repository.ChatRepository
import com.sushil.chatapp.utils.PrefManager
import kotlinx.coroutines.launch

class AuthViewModel(val context: Context) : ViewModel() {


    val codeSent = MutableLiveData<Boolean>()
    val signInSuccess = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    private val repository = AuthRepository(context)

    val authState: LiveData<FirebaseUser?> = repository.authState

    fun logout() {
        repository.logout()
    }

    private val _isConnected = MutableLiveData<Boolean>(false)
    val isConnected: LiveData<Boolean> get() = _isConnected

    fun updateConnectivity(connectionFlag: Boolean) {
        viewModelScope.launch {
            try {
                _isConnected.value = connectionFlag
            } catch (e: Exception) {

            }
        }
    }

    fun sendOtp(phone: String, activity: Activity) {
        repository.startPhoneAuth(phone, activity, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                error.postValue(e.localizedMessage)
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                repository.setVerificationId(verificationId)
                codeSent.postValue(true)
            }
        })
    }

    fun getUserSession(): Boolean {

        return repository.checkUserSession()
    }

    fun verifyCode(code: String) {
        val credential = repository.getCredential(code)
        if (credential != null) {
            signInWithCredential(credential)
        } else {
            error.postValue("Invalid verification ID")
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        repository.signInWithCredential(credential, {
            signInSuccess.postValue(true)
        }, {
            error.postValue(it)
        })
    }

    override fun onCleared() {
        super.onCleared()
        repository.cleanup() // Optional
    }
}