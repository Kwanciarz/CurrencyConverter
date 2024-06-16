package com.adam.currencyconverterroom.data

import android.util.Log
import com.adam.currencyconverterroom.data.database.CurrencyDao
import com.adam.currencyconverterroom.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow


class CurrencyRepository(
    private val databaseDao: CurrencyDao,
    private val remoteDataSource: RemoteDataSource
) {
    
    //Network
    suspend fun fetchAndStoreAllRates(listOfCodes: List<String>) {
            val networkResponse = remoteDataSource.fetchCurrenciesRates(listOfCodes)
        Log.v("NetworkResponse", "above get null ${networkResponse.toString()}")
        networkResponse.fold(
            onSuccess = {
                for (networkCurrency in it) {
                    val listOfRates = convertNetworkCurrencyToDatabaseEntities(networkCurrency)
                    databaseDao.addRates(listOfRates)
                }
            }
            , onFailure = {
                //TODO handle errors
            }
        )
    }
    suspend fun fetchAndStoreRatesForNewEntries(oldCodes: List<String>, newCodes: List<String>) {
        val networkResponse = remoteDataSource.fetchForExistingRates(oldCodes, newCodes)
        networkResponse.fold(
            onSuccess = {
               for (networkCurrency in it) {
                   val listOfRates = convertNetworkCurrencyToDatabaseEntities(networkCurrency)
                   databaseDao.addRates(listOfRates)
               }
            },
            onFailure = {
                //TODO handle errs
            }
        )
    }
    
    //Database + network
    //add currency
    suspend fun addCurrencies(oldCodes: List<String>, newCodes: List<String>) {
        val response = remoteDataSource.fetchForExistingRates(oldCodes, newCodes)
        response.fold(
            onSuccess = {
                for (networkCurrency in it) {
                    val listOfRates = convertNetworkCurrencyToDatabaseEntities(networkCurrency)
                    databaseDao.addRates(listOfRates)
                }
            },
            onFailure = {
                //TODO handle errors in adding
            }
        )
    }
    
    suspend fun fetchAllAvailableCurrencies(): Result<List<CurrencyWithName>> {
//        val response =
            return remoteDataSource.fetchAvailableCurrencies()
//        Log.v("Fetch"," response $response")
//        response.fold(
//            onSuccess = {return Result.success(it)},
//            onFailure = {return Result.failure(it)}
//        )
    }
    
    //Database
    fun getRatesForSelectedCurrency(currencyCode: String): Flow<List<CurrencyWithRate>> {
        return databaseDao.getSelectedCurrencyRates(currencyCode)
    }
    fun getCodes():Flow<List<String>>{
        return databaseDao.getCodes()
    }
    suspend fun deleteByCode(code:String){
        databaseDao.deleteRatesWithBaseCode(code)
    }
}