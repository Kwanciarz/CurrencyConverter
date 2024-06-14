package com.adam.currencyconverterroom

import android.app.Application
import com.adam.currencyconverterroom.data.CurrencyRepository
import com.adam.currencyconverterroom.data.database.CurrencyDatabase
import com.adam.currencyconverterroom.data.remote.RemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CurrencyConverterApplication: Application(){
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy{CurrencyDatabase.getDatabase(this, applicationScope)}
    val repository by lazy{CurrencyRepository(database.currencyDao(), RemoteDataSource()) }
//    override fun onCreate() {
//        super.onCreate()
//        runBlocking {
//            val list = database.currencyDao().getListOfSelectedCurrencies().first()
//            repository.fetchAndStoreAllRates(list.map{it.base})
//        }
//    }

}