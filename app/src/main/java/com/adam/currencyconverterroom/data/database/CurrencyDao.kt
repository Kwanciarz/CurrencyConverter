package com.adam.currencyconverterroom.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adam.currencyconverterroom.data.ChosenCurrency
import com.adam.currencyconverterroom.data.CurrencyWithRate
import com.adam.currencyconverterroom.data.NetworkMetadata
import com.adam.currencyconverterroom.data.Selected
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM rates WHERE base = :selectedCode")
    fun getSelectedCurrencyRates(selectedCode: String): Flow<List<CurrencyWithRate>>
    
    @Query("SELECT * FROM rates WHERE base = :selectedCode")
    fun getRatesOneShot(selectedCode: String): List<CurrencyWithRate>
    
    @Query("SELECT selectedCode FROM selected")
    fun getSelectedCode(): Flow<String>
   
    //   @Query("INSERT INTO selected VALUES(:code)")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun selectCode(selected: Selected)
    
    @Query("SELECT * FROM chosen_names")
    fun getListOfSelectedCurrencies(): Flow<List<ChosenCurrency>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCurrencyToListOfSelected(code: ChosenCurrency)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCurrenciesToListOfSelected(codes: List<ChosenCurrency>)
    
    @Query("DELETE FROM chosen_names where base = :baseCode")
    suspend fun deleteFromNamesList(baseCode: String)
    
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRate(rate: CurrencyWithRate)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRates(rates: List<CurrencyWithRate>)
    
    @Query("DELETE FROM rates WHERE base = :baseCode OR target = :baseCode")
    suspend fun deleteRatesWithBaseCode(baseCode: String)
    
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(value: NetworkMetadata)
    
    @Query("SELECT lastUpdated FROM metadata")
    suspend fun getMetadata(): String
}