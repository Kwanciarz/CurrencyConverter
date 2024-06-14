package com.adam.currencyconverterroom.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
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
            
            if (isApiResponseOutdated(repository.getDateOfQuery())) {
                val listOfCodes = repository.getSelectedCodes().first().map { it.base }
                Log.v("ViewModelFetch", listOfCodes.toString())
                repository.fetchAndStoreAllRates(listOfCodes)
            }
        }
    suspend fun fetchDataForNewEntries(oldCodes: List<String>, newCodes: List<String>) = viewModelScope.launch {
       repository.fetchAndStoreRatesForNewEntries(oldCodes, newCodes)
    }
    
    //get all rates for selected currency
    val selectedCurrency: StateFlow<String> = repository.getSelectedCurrency().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "" // Provide an appropriate initial value
    )
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val ratesForSelectedCurrency: Flow<List<CurrencyWithRate>> =
        selectedCurrency.flatMapLatest { currency ->
            if (currency == "") {
                flowOf(emptyList())
            } else {
                repository.getRatesForSelectedCurrency(currency)
            }
        }
    
    fun getRateOneShot(code: String): List<CurrencyWithRate> {
        return repository.getRatesOneShot(code)
    }
    
    
    fun selectCode(code: String) = viewModelScope.launch {
        repository.selectCode(code)
    }
    
    fun getListOfSelectedCodes(): Flow<List<ChosenCurrency>> {
        return repository.getSelectedCodes()
    }
    
    //delete currency
    fun deleteCurrency(code: String) = viewModelScope.launch {
        repository.deleteCurrency(code)
    }
    
    //add currency
    suspend fun addCurrencies(oldCodes: List<String>, newCodes: List<String>) =
        viewModelScope.launch {
            repository.addCurrencies(oldCodes, newCodes)
        }
    
    val getAllAvailableCurrencies:Flow<List<CurrencyWithName>> = flow<List<CurrencyWithName>> {
        emit(repository.fetchAllAvailableCurrencies())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    
    //set date of query
    fun setDateOfQuery(date: String) = viewModelScope.launch {
        repository.setDateOfQuery(date)
    }
    
    init {
        viewModelScope.launch(Dispatchers.IO) {
            Log.v("ViewModelInit", "GETS HERE")
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
