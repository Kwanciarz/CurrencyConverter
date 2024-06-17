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
    
    fun saveDateOfQuery(date: String) {
        prefs.edit().putString("date_query", date).apply()
    }
    
    fun getDateOfLastQuery(): String {
        return prefs.getString("date_query", "1970-01-01") ?: "1970-01-01"
    }
}