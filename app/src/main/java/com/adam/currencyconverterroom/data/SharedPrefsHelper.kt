package com.adam.currencyconverterroom.data

import android.content.Context

class SharedPrefsHelper(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    fun saveSelectedCode(code: String) {
        prefs.edit().putString("selected_code", code).apply()
    }
    
    fun getSelectedCode(defaultCode: String): String {
        return prefs.getString("selected_code", defaultCode) ?: defaultCode
    }
    
    fun saveLastFetchDate(date: String) {
        prefs.edit().putString("last_fetch_date", date).apply()
    }
    
    fun getLastFetchDate(): String {
        return prefs.getString("last_fetch_date", "") ?: ""
        
    }

}