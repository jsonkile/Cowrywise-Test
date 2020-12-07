package com.golde.cowrywise.repos

import android.util.Log
import androidx.lifecycle.LiveData
import com.golde.cowrywise.db.RatesDao
import com.golde.cowrywise.models.Rate
import com.golde.cowrywise.models.Currency
import com.golde.cowrywise.network.FixerService
import com.haroldadmin.cnradapter.NetworkResponse

class RatesRepositoryImpl(private val dao: RatesDao, private val fixerService: FixerService) :
    RatesRepository {
    /**
     * Get response from API and convert to Application readable Rates before adding to DB
     * If the request failed, Log error
     */
    override suspend fun getAndStoreRatesFromAPI(key: String) {
        val result =
            fixerService.getRates(key)

        when (result) {
            is NetworkResponse.Success -> {
                val sexyRates = mutableListOf<Rate>()
                for (t in result.body.rates) {
                    sexyRates.add(Rate(to = t.key, rate = t.value.toFloat()))
                }
                dao.storeRate(sexyRates)
            }
            else -> Log.d("ERROR", "ERROR  FETCHING RATES")
        }
    }

    /**
     * Get response from API and convert to Application readable currencies before adding to DB
     * If the request failed, Log error
     */
    override suspend fun getCurrenciesAndSymbols(key: String) {
        val result =
            fixerService.getSymbols(key)

        when (result) {
            is NetworkResponse.Success -> {
                val sexySymbols = mutableListOf<Currency>()
                for (t in result.body.symbols) {
                    sexySymbols.add(Currency(currency = t.key, nation = t.value))
                }
                dao.storeCurrency(sexySymbols)
            }
            else -> Log.d("ERROR", "ERROR  FETCHING SYMBOLS")
        }

    }

    override fun getRatesFromDB(): LiveData<List<Rate>> {
        return dao.getRates()
    }

    override fun getCurrenciesFromDB(): LiveData<List<Currency>> {
        return dao.getCurrencies()
    }

    override suspend fun getHistoricRate(
        key: String,
        base: String,
        date: String
    ): Map<String, Float> {
        val result =
            fixerService.getHistoricalRate(date, base, key)

        return when (result) {
            is NetworkResponse.Success -> {
                result.body.rates
            }
            else -> {
                Log.d("ERROR", "ERROR  FETCHING SYMBOLS")
                emptyMap()
            }
        }
    }
}