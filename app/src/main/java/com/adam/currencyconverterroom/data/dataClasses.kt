package com.adam.currencyconverterroom.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
@Serializable
@Entity(tableName = "metadata")
data class NetworkMetadata(
    @PrimaryKey val id: Int = 0, // Single row for metadata
    val lastUpdated: String ="1-1-1970"
)

@Serializable
@Entity(
    tableName = "selected"
)
data class Selected(
    @PrimaryKey
    val id:Int = 0,
    var selectedCode:String
)

@Serializable
@Entity(
    tableName = "chosen_names"
)
data class ChosenCurrency(
    @PrimaryKey
    var base:String
)

@Serializable
@Entity(
    tableName = "rates",
    primaryKeys =  ["base","target"]
)
data class CurrencyWithRate(
    val base: String,
    val target:String,
    var rate:Double,
)
@Serializable
data class NetworkCurrencyWithRates(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
@Serializable
data class CurrencyWithName(
    val base: String,
    val name: String)

//
//data class CurrencyUiState(
//   val rates : List<CurrencyWithRate> = emptyList(),
//    val predicate: String = ""
//)