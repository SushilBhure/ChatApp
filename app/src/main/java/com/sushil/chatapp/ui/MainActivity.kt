package com.sushil.chatapp.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.sushil.chatapp.R
import com.sushil.chatapp.databinding.ActivityMainBinding
import com.sushil.chatapp.utils.NetworkUtils
import com.sushil.chatapp.utils.PrefManager
import com.sushil.chatapp.utils.showToast
import com.sushil.chatapp.viewmodels.AuthViewModel
import com.sushil.chatapp.viewmodels.AuthViewModelFactory
import com.sushil.chatapp.viewmodels.UserViewModel

class MainActivity : AppCompatActivity(), NetworkUtils.NetworkListener {

    private var noInternetDialog: AlertDialog? = null
    private val userViewModel: UserViewModel by viewModels()
    private val authViewModel: AuthViewModel by  viewModels {
        AuthViewModelFactory(this)
    }

    private val navController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val bottomNavFragments = setOf(
        R.id.chatFragment,
        R.id.statusFragment,
        R.id.profileFragment
    )

    private val skipStatusFragments = listOf(
        R.id.splashFragment,
        R.id.loginFragment,
        R.id.registerFragment
    )

    override fun onResume() {
        super.onResume()

        NetworkUtils.registerNetworkCallback(this, this)

        val currentDest = navController.currentDestination?.id

        if(!PrefManager.getUserId(this).isNullOrBlank()){
            if (currentDest !in skipStatusFragments) {
                userViewModel.updateUserStatus(PrefManager.getUserId(this).toString(),true)
            }else{
                userViewModel.updateUserStatus(PrefManager.getUserId(this).toString(),false)
            }
        }
    }

    override fun onStop() {
        super.onStop()

        NetworkUtils.unregisterNetworkCallback(this)

        if(!PrefManager.getUserId(this).isNullOrBlank()){
            userViewModel.updateUserStatus(PrefManager.getUserId(this).toString(),false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isNetworkAvailable()) {
            showNoInternetDialog()
        }

        setContentView(binding.root)

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible = destination.id in bottomNavFragments

            if(!PrefManager.getUserId(this).isNullOrBlank()){
                if ((destination.id !in skipStatusFragments)) {
                    userViewModel.updateUserStatus(PrefManager.getUserId(this).toString(),true)
                }else{
                    userViewModel.updateUserStatus(PrefManager.getUserId(this).toString(),false)
                }
            }
        }

        // Observe user state
        authViewModel.authState.observe(this) { user ->
            if (user == null) {
                val navController = findNavController(R.id.nav_host_fragment)
                val currentDest = navController.currentDestination?.id

                if (currentDest != R.id.splashFragment) {
                    showToast("User logged out..!")
                    PrefManager.clearUserId(this)
                    navController.navigate(
                        R.id.splashFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(0, true) // 0 means clear everything
                            .build()
                    )
                }
            }
        }

    }

    override fun onNetworkAvailable() {
        // Dismiss the dialog only if it's currently showing
        authViewModel.updateConnectivity(true)
        runOnUiThread({

            if (noInternetDialog?.isShowing == true) {
                noInternetDialog?.dismiss()
                noInternetDialog = null
            }
        })
    }

    override fun onNetworkLost() {
        // Show dialog only if not already showing or context is not finishing
        runOnUiThread({
            showNoInternetDialog()
            })
    }

    private fun showNoInternetDialog() {
        authViewModel.updateConnectivity(false)
        if (noInternetDialog?.isShowing != true && !isFinishing) {
            noInternetDialog = AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Internet is required to use the app. Please connect or exit.")
                .setCancelable(false)
                .setPositiveButton("Exit App") { _, _ ->
                    finishAffinity()
                }
                .create()
            noInternetDialog?.show()
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                authViewModel.logout() // Trigger Firebase signOut
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    fun showLogoutPopupMenu(anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView) // `it` is the anchor view
        popupMenu.menu.add("Logout")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Logout" -> {
                   logout()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}