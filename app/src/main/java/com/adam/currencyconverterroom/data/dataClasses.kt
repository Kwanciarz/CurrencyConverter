package com.adam.currencyconverterroom.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
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