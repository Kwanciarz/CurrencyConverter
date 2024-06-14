package com.adam.currencyconverterroom.data



fun convertNetworkCurrencyToDatabaseEntities(jsonCurrency:NetworkCurrencyWithRates):List<CurrencyWithRate>{
    val list = mutableListOf<CurrencyWithRate>()
    jsonCurrency.rates.forEach {
        list.add(CurrencyWithRate(jsonCurrency.base,it.key,it.value))
    }
    return list
}