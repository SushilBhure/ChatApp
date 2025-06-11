package com.sushil.chatapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushil.chatapp.models.User
import com.sushil.chatapp.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error


    fun updateUserStatus(currentUserID: String, status: Boolean) {
        viewModelScope.launch {
            repository.updateUserStatus(currentUserID, status)
        }
    }

    fun updateUserName(currentUserID: String, name: String) {
        viewModelScope.launch {
            repository.updateUserName(currentUserID, name)
        }
    }

    fun updateUserProfilePic(currentUserID: String, imgUrl: String) {
        viewModelScope.launch {
            repository.updateProfileImage(currentUserID, imgUrl)
        }
    }

    fun getUser(number: String) {
        viewModelScope.launch {
            try {
                _user.value = repository.getUser(number)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

   /* fun getUser(number: String): LiveData<User?> {
        return repository.getUserLiveData(number)
    }*/

     suspend fun isUserRegistered(number: String): Boolean {

            return repository.isUserRegistered(number)
    }

    fun FetchAllUsers(){

    }

    fun createUser(number: String, user: User) {
        viewModelScope.launch {
            try {
                repository.createUser(number, user)
               // fetchUsers()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}