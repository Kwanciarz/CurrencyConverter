package com.adam.currencyconverterroom.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adam.currencyconverterroom.data.CurrencyWithRate
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM rates WHERE base = :selectedCode")
    fun getSelectedCurrencyRates(selectedCode: String): Flow<List<CurrencyWithRate>>
    
    @Query("SELECT DISTINCT base FROM rates")
    fun getCodes():Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRate(rate: CurrencyWithRate)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRates(rates: List<CurrencyWithRate>)
    
    @Query("DELETE FROM rates WHERE base = :baseCode OR target = :baseCode")
    suspend fun deleteRatesWithBaseCode(baseCode: String)
}