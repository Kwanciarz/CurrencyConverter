package com.adam.currencyconverterroom.data

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adam.currencyconverterroom.data.remote.ConnectivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CurrencyViewModel(private val repository: CurrencyRepository, private val connectivityRepository: ConnectivityRepository,private val prefsHelper: SharedPrefsHelper) : ViewModel() {
    
//    private fun isApiResponseOutdated(): Boolean {
//        val lastFetchDateStr = prefsHelper.getLastFetchDate()
//        if (lastFetchDateStr.isEmpty()) return true
//
//        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val lastFetchDate = sdf.parse(lastFetchDateStr) ?: return true
//        val today = Date()
//
//        // Check if it's a working day (Mon-Fri) AND past 16:00 CET
//        val cet = TimeZone.getTimeZone("CET")
//        val calendar = Calendar.getInstance(cet)
//        calendar.time = today
//        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
//        val isWorkingDay = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
//        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
//        val isPastRefreshTime = hourOfDay >= 16
//
//        // Outdated if:
//        // 1. It's a working day, past refresh time, and last fetch was BEFORE today
//        // 2. OR, it's a working day, past refresh time, and last fetch was TODAY but BEFORE 16:00 CET
//        // 3. OR, last fetch was before the last working day
//        return (isWorkingDay && isPastRefreshTime && lastFetchDate.before(today)) ||
//                (isWorkingDay && isPastRefreshTime && sameDay(lastFetchDate, today) && !isAfterRefreshTime(lastFetchDate)) ||
//                lastFetchDate.before(getLastWorkingDay(today))
//    }
//
//    // Helper functions
//    private fun sameDay(date1: Date, date2: Date): Boolean {
//        val cal1 = Calendar.getInstance().apply { time = date1}
//        val cal2 = Calendar.getInstance().apply { time = date2 }
//        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
//    }
//
//    private fun isAfterRefreshTime(date: Date): Boolean {
//        val cet = TimeZone.getTimeZone("CET")
//        val calendar = Calendar.getInstance(cet).apply { time = date }
//        return calendar.get(Calendar.HOUR_OF_DAY) >= 16
//    }
//
//    private fun getLastWorkingDay(date: Date): Date {
//        val calendar = Calendar.getInstance()
//        calendar.time = date
//
//        do {
//            calendar.add(Calendar.DAY_OF_MONTH, -1)
//        } while (calendar.get(Calendar.DAY_OF_WEEK) in arrayOf(Calendar.SATURDAY, Calendar.SUNDAY))
//
//        return calendar.time
//    }
    
    suspend fun fetchAndStoreAllRatesIfOutdated(/*listOfCodes: List<String>*/) =
        viewModelScope.launch {
            if (true){
                val listOfCodes = repository.getCodes().first()
                repository.fetchAndStoreAllRates(listOfCodes)
                val currentDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                prefsHelper.saveLastFetchDate(currentDateStr)
                Log.v("ViewModelFetch","Fetched")
            }
            
            Log.v("ViewModelFetch","Outside")
        }
    
    suspend fun fetchDataForNewEntries(oldCodes: List<String>, newCodes: List<String>) =
        viewModelScope.launch {
            repository.fetchAndStoreRatesForNewEntries(oldCodes, newCodes)
        }
    val codes = repository.getCodes()
    
    private val _availableCurrencies: MutableStateFlow<List<CurrencyWithName>> =
        MutableStateFlow(emptyList())
    val availableCurrencies: StateFlow<List<CurrencyWithName>> = _availableCurrencies.asStateFlow()
    fun fetchAllAvailableCurrencies(){
       viewModelScope.launch {
            repository.fetchAllAvailableCurrencies().fold(
                onSuccess = { nameList ->
                    _availableCurrencies.update {nameList}
                },
                onFailure = {
                   _availableCurrencies.update { emptyList() }
                }
            )
       }
    }
    
    private val _ratesBySelectedCode = MutableStateFlow<List<CurrencyWithRate>>(emptyList())
    val ratesBySelectedCode: StateFlow<List<CurrencyWithRate>> = _ratesBySelectedCode.asStateFlow()
    
    fun updateSelectionOfRate(selectedCode: String) {
        viewModelScope.launch {
            repository.getRatesForSelectedCurrency(selectedCode).collect { rates ->
                _ratesBySelectedCode.value = rates
            }
        }
    }
    
    
    fun deleteCurrency(code:String)=
        viewModelScope.launch {
            repository.deleteByCode(code)
        }
    
    //add currency
//    suspend fun addCurrencies(oldCodes: List<String>, newCodes: List<String>) =
//        viewModelScope.launch {
//            repository.addCurrencies(oldCodes, newCodes)
//        }
    
    private val _amount = MutableStateFlow("10")
    val amount: StateFlow<String> = _amount.asStateFlow()
    
    // Function to update the amount in the ViewModel
    fun updateAmount(newAmount: String) {
        _amount.value = newAmount
    }
    
    private val _isInternet = MutableStateFlow<Boolean>(true)
    val isInternet = _isInternet.asStateFlow()
    
    fun getInternetState(): Flow<Boolean> = connectivityRepository.isConnected
    
    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchAndStoreAllRatesIfOutdated()
        }
    }
    
    class WordViewModelFactory(private val repository: CurrencyRepository, private val connectivityRepository: ConnectivityRepository,private val prefsHelper: SharedPrefsHelper) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CurrencyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CurrencyViewModel(repository, connectivityRepository,prefsHelper) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
