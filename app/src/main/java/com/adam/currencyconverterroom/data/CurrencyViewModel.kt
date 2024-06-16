package com.adam.currencyconverterroom.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CurrencyViewModel(private val repository: CurrencyRepository) : ViewModel() {
    private fun isApiResponseOutdated(apiResponseDate: String): Boolean {
        // Parse the API response date
        val formatter = DateTimeFormatter.ISO_DATE
        val apiDate = LocalDate.parse(apiResponseDate, formatter)
        
        // Get current time in CET
        val cetZone = ZoneId.of("CET")
        val nowCet = ZonedDateTime.now(cetZone)
        
        // Check if today is a working day
        if (nowCet.dayOfWeek == DayOfWeek.SATURDAY || nowCet.dayOfWeek == DayOfWeek.SUNDAY) {
            return true // Data might not be fresh on weekends
        }
        
        // Check if API response date is today and if it's past 16:00 CET
        return if (apiDate == nowCet.toLocalDate() && nowCet.toLocalTime()
                .isAfter(LocalTime.of(16, 0))
        ) {
            false // Data is likely fresh
        } else {
            true // Data might be outdated
        }
    }
    
    suspend fun fetchAndStoreAllRatesIfOutdated(/*listOfCodes: List<String>*/) =
        viewModelScope.launch {
            if (true) { //isApiResponseOutdated TODO check it
                val listOfCodes = repository.getCodes().first()
                Log.v(
                    "ViewModelFetch",
                    "List of codes while fetching data${listOfCodes.toString()}"
                )
                repository.fetchAndStoreAllRates(listOfCodes)
            }
        }
    
    suspend fun fetchDataForNewEntries(oldCodes: List<String>, newCodes: List<String>) =
        viewModelScope.launch {
            repository.fetchAndStoreRatesForNewEntries(oldCodes, newCodes)
        }
    val codes = repository.getCodes()
    
    val _availableCurrencies: MutableStateFlow<List<CurrencyWithName>> =
        MutableStateFlow(emptyList())
    val availableCurrencies: StateFlow<List<CurrencyWithName>> = _availableCurrencies.asStateFlow()
    fun fetchAllAvailableCurrencies(){
       viewModelScope.launch {
            val result = repository.fetchAllAvailableCurrencies().fold(
                onSuccess = {
                    _availableCurrencies.update {it}
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
    
    //add currency
    suspend fun addCurrencies(oldCodes: List<String>, newCodes: List<String>) =
        viewModelScope.launch {
            repository.addCurrencies(oldCodes, newCodes)
        }
    
    
    init {
        viewModelScope.launch(Dispatchers.IO) {
            Log.v("ViewModelInit", "View model is created")
            fetchAndStoreAllRatesIfOutdated()
            
        }
    }
    
    class WordViewModelFactory(private val repository: CurrencyRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CurrencyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CurrencyViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
