package com.adam.currencyconverterroom.data

import android.util.Log
import com.adam.currencyconverterroom.data.database.CurrencyDao
import com.adam.currencyconverterroom.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow


class CurrencyRepository(
    private val databaseDao: CurrencyDao,
    private val remoteDataSource: RemoteDataSource
) {
    
    suspend fun fetchAndStoreAllRates(listOfCodes: List<String>): Boolean {
        val networkResponse = remoteDataSource.fetchCurrenciesRates(listOfCodes)
        Log.v("NetworkResponse", "above get null ${networkResponse.toString()}")
        networkResponse.getOrNull()?.let { response ->
            Log.v("NetworkResponse", "below null check ${response.toString()}")
            for (networkCurrency in response) {
                val listOfRates = convertNetworkCurrencyToDatabaseEntities(networkCurrency)
                databaseDao.addRates(listOfRates)
            }
            if (response.isNotEmpty()) {
                databaseDao.insertMetadata(NetworkMetadata(0,response.first().date))
            }
            return true
        }
        Log.e("NetworkResponse", "Error")
        return false
    }
    suspend fun fetchAndStoreRatesForNewEntries(oldCodes: List<String>, newCodes: List<String>):Boolean {
        val networkResponse = remoteDataSource.fetchForExistingRates(oldCodes, newCodes)
        networkResponse.getOrNull()?.let { response ->
            for (networkCurrency in response) {
                val listOfRates = convertNetworkCurrencyToDatabaseEntities(networkCurrency)
                databaseDao.addRates(listOfRates)
            }
            return true
        }
        return false
    }
    
    fun getSelectedCodes():Flow<List<ChosenCurrency>>{
        return databaseDao.getListOfSelectedCurrencies()
    }
    
    //get all rates for selected currency
    fun getRatesForSelectedCurrency(currencyCode: String): Flow<List<CurrencyWithRate>> {
        return databaseDao.getSelectedCurrencyRates(currencyCode)
    }
    fun getRatesOneShot(code: String):List<CurrencyWithRate>{
        return databaseDao.getRatesOneShot(code)
    }
    
    //get selected currency
    fun getSelectedCurrency(): Flow<String> {
        return databaseDao.getSelectedCode()
    }
    
    //select currency
   suspend fun selectCode(code: String) {
        databaseDao.selectCode(Selected(0,code))
    }
    
    //delete currency
   suspend fun deleteCurrency(code: String) {
        databaseDao.deleteFromNamesList(code)
        databaseDao.deleteRatesWithBaseCode(code)
    }
    
    //add currency
    suspend fun addCurrencies(oldCodes: List<String>, newCodes: List<String>): Boolean {
        val response = remoteDataSource.fetchForExistingRates(oldCodes, newCodes)
        response.getOrNull()?.let { networkResponse ->
            for (networkCurrency in networkResponse) {
                val listOfRates = convertNetworkCurrencyToDatabaseEntities(networkCurrency)
                databaseDao.addRates(listOfRates)
            }
            val newCodesForDatabaseFormat = newCodes.map { code -> ChosenCurrency( code) }
            databaseDao.addCurrenciesToListOfSelected(newCodesForDatabaseFormat)
            return true
        }
        return false
    }
    suspend fun fetchAllAvailableCurrencies(): List<CurrencyWithName> {
        val response = remoteDataSource.fetchAvailableCurrencies()
        Log.v("Fetch"," response $response")
         response.fold(
            onSuccess = {return it},
            onFailure = {return emptyList()}
        )
    }
    //get date of query
    
    suspend fun getDateOfQuery(): String{
        return databaseDao.getMetadata()
    }
    
    //set date of query
    suspend fun setDateOfQuery(date: String) {
     databaseDao.insertMetadata(NetworkMetadata(0,date))
    }
    
}