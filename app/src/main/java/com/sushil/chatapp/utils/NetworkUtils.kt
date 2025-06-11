package com.sushil.chatapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network

object NetworkUtils {

    interface NetworkListener {
        fun onNetworkAvailable()
        fun onNetworkLost()
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun registerNetworkCallback(context: Context, listener: NetworkListener) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                listener.onNetworkAvailable()
            }

            override fun onLost(network: Network) {
                listener.onNetworkLost()
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
    }

    fun unregisterNetworkCallback(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
    }
}