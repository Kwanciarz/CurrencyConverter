package com.adam.currencyconverterroom.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.adam.currencyconverterroom.data.ChosenCurrency
import com.adam.currencyconverterroom.data.CurrencyWithRate
import com.adam.currencyconverterroom.data.NetworkMetadata
import com.adam.currencyconverterroom.data.Selected
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Database class with a singleton Instance object.
 */
@Database(
    entities = [ChosenCurrency::class, CurrencyWithRate::class, Selected::class, NetworkMetadata::class],
    version = 1,
    exportSchema = false
)
abstract class CurrencyDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    
    companion object {
        @Volatile
        private var Instance: CurrencyDatabase? = null
        
        fun getDatabase(context: Context, scope: CoroutineScope): CurrencyDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CurrencyDatabase::class.java,"currencyDatabase")
                    .fallbackToDestructiveMigration()
                    .addCallback(CurrencyDatabaseCallback(scope))
                    .build()
                    .also { Instance = it }
            }
        }
    }
    
    
    private class CurrencyDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Instance?.let { database ->
                scope.launch {
                    populateDatabase(database.currencyDao())
                }
            }
        }
        
        suspend fun populateDatabase(dao: CurrencyDao) {
            dao.insertMetadata(NetworkMetadata(0,"1970-01-01"))
            dao.addCurrenciesToListOfSelected(
                listOf(
                    ChosenCurrency("PLN"),
                    ChosenCurrency("EUR"),
                    ChosenCurrency("USD")
                )
            )
            dao.selectCode(Selected(0, "PLN"))
            dao.addRates(
                listOf(
                    CurrencyWithRate("PLN", "USD",0.0),
                    CurrencyWithRate("PLN", "EUR",0.0),
                    
                    CurrencyWithRate("EUR", "PLN",0.0),
                    CurrencyWithRate("EUR", "USD",0.0),
                    
                    CurrencyWithRate("USD", "PLN",0.0),
                    CurrencyWithRate("USD", "EUR",0.0),
                )
            )
            
        }
    }
}
