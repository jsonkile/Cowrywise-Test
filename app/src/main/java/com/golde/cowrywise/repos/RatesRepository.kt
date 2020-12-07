package com.golde.cowrywise.repos

import androidx.lifecycle.LiveData
import com.golde.cowrywise.models.Rate
import com.golde.cowrywise.models.Currency

interface RatesRepository {
    suspend fun getAndStoreRatesFromAPI(key: String)

    suspend fun getCurrenciesAndSymbols(key: String)

    fun getRatesFromDB(): LiveData<List<Rate>>

    fun getCurrenciesFromDB(): LiveData<List<Currency>>

    suspend fun getHistoricRate(key: String, base: String, date: String) : Map<String, Float>
}