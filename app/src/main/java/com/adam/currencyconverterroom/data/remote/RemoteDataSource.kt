package com.adam.currencyconverterroom.data.remote

import android.util.Log
import com.adam.currencyconverterroom.data.CurrencyWithName
import com.adam.currencyconverterroom.data.CurrencyWithRate
import com.adam.currencyconverterroom.data.NetworkCurrencyWithRates
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json



//sealed class QueryResult<out T>{
//    data class Success<out T>(val data: T): QueryResult<T>()
//    data class Error(val exception: Exception): QueryResult<Nothing>()
//    data object Loading: QueryResult<Nothing>()
//}

class RemoteDataSource {
    private val client =
        HttpClient() {
            install(ContentNegotiation) {
                json()
            }
        }
    private val baseUrl = "https://api.frankfurter.app/"
    suspend fun fetchAvailableCurrencies(): Result<List<CurrencyWithName>> {
        return try {
            val url = "${baseUrl}currencies"
            val response: String = client.get(url).body()
            val json = Json { ignoreUnknownKeys = true }
            val currencies = json.decodeFromString<Map<String, String>>(response)
                .map { CurrencyWithName(it.key, it.value) }
            Log.v("Network","All : ${currencies}")
            Result.success(currencies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchCurrenciesRates(selectedCodes: List<String>): Result<List<NetworkCurrencyWithRates>> {
        return try {
            val resultList = mutableListOf<NetworkCurrencyWithRates>()
            for (currency in selectedCodes) {
                val targetCurrenciesString =
                    selectedCodes.filter { it != currency }.joinToString(",")
                val url = "${baseUrl}latest?from=$currency&to=$targetCurrenciesString"
                val response: String = client.get(url).body()
                val json = Json { ignoreUnknownKeys = true }
                val currencyRates = json.decodeFromString<NetworkCurrencyWithRates>(response)
                Log.v("Network","fetched $currencyRates")
                resultList.add(currencyRates)
            }
            Result.success(resultList)
        } catch (e: Exception) {
            Log.e("Network","error $e")
            Result.failure(e)
        }
    }
   /*
   * old PLN,USD,EUR
   * new GBP,CZK
   *
   * in database
   * pln - usd
   * pln - eur
   * usd- pln
   * usd - eur
   * eur- pln
   * eur- usd
   *
   * Need to add
   * GPB - [PLN,USD,EUR,CZK]
   * CZK - [PLN,USD,EUR,GBP]
   *
   * PLN - [GBP,CZK]
   * USD - [GBP,CZK]
   * EUR- [GBP,CZK]
   *
   * */
    suspend fun fetchForExistingRates(oldCodes: List<String>, newCodes: List<String>): Result<List<NetworkCurrencyWithRates>> {
        return try {
            val resultList = mutableListOf<NetworkCurrencyWithRates>()
            for(newCode in newCodes){
                val targetCurrenciesString =
                    (newCodes+oldCodes).filter { it != newCode }.joinToString(",")
                val url = "${baseUrl}latest?from=$newCode&to=$targetCurrenciesString"
                val response: String = client.get(url).body()
                val json = Json { ignoreUnknownKeys = true }
                val currencyRates = json.decodeFromString<NetworkCurrencyWithRates>(response)
                resultList.add(currencyRates)
            }
            for (oldCode in oldCodes) {
                val targetCurrenciesString =
                    newCodes.joinToString(",")
                val url = "${baseUrl}latest?from=$oldCode&to=$targetCurrenciesString"
                val response: String = client.get(url).body()
                val json = Json { ignoreUnknownKeys = true }
                val currencyRates = json.decodeFromString<NetworkCurrencyWithRates>(response)
                resultList.add(currencyRates)
            }
            Result.success(resultList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
}
