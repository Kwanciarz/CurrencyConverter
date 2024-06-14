package com.adam.currencyconverterroom.data.remote
//https://khush7068.medium.com/how-to-observe-internet-connectivity-in-android-modern-way-with-kotlin-flow-7868a322c806
//By Anubhav Sharma

import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class ConnectivityRepository(context: Context) {
    
  private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: Flow<Boolean> = _isConnected
    
    init {
        // Observe network connectivity changes
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                _isConnected.value = true
            }
            
            override fun onLost(network: android.net.Network) {
                _isConnected.value = false
            }
        })
    }
}