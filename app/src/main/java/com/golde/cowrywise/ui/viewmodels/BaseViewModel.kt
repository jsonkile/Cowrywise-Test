package com.golde.cowrywise.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.golde.cowrywise.helpers.API_KEY
import com.golde.cowrywise.helpers.PREF_KEY
import com.golde.cowrywise.models.Currency
import com.golde.cowrywise.models.Rate
import com.golde.cowrywise.repos.RatesRepository
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.parse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BaseViewModel(private val repository: RatesRepository, application: Application) :
    AndroidViewModel(application) {

    //Live data of rates from DB
    var rates: LiveData<List<Rate>> = repository.getRatesFromDB()
    //Live data of currencies from DB
    var currencies: LiveData<List<Currency>> = repository.getCurrenciesFromDB()

    //Live data holding historic rates after network request
    var historicRates = MutableLiveData<List<Pair<String, Float>>>(null)

    //For error cases
    var errorMessage = MutableLiveData("")

    //Live data for base, target currencies and amounts with default values
    var base = MutableLiveData("USD")
    var target = MutableLiveData("EUR")
    var baseAmount = MutableLiveData(1F)
    var targetAmount = MutableLiveData(1F)

    //Function to get latest rates from Repo
    fun getLatestRates() {
        viewModelScope.launch {
            getAPIKeyFromSharedPref()?.let {
                launch(Dispatchers.IO) {
                    repository.getAndStoreRatesFromAPI(it)
                }
            }
        }
    }

    //Function to get symbols from Repo
    fun getSymbols() {
        viewModelScope.launch {
            getAPIKeyFromSharedPref()?.let {
                launch(Dispatchers.IO) {
                    repository.getCurrenciesAndSymbols(it)
                }

            }
        }
    }

    //When the convert button is tapped, the amount (baseEntry) of base currency is passed and used to calculate target currency
    fun convert(baseEntry: Float) {
        if (base.value == "USD") {
            val conversionRate = rates.value?.firstOrNull { it.to == target.value }?.rate
            if (conversionRate != null) targetAmount.postValue(baseEntry * conversionRate)
        } else {
            /**
             * Cross convert for other currencies
             * Convert to dollars then to target currency
             */
            val conversionRateBetweenBaseCurrencyAndDollars =
                rates.value?.firstOrNull { it.to == base.value }?.rate
            if (conversionRateBetweenBaseCurrencyAndDollars != null) {
                val dollarValue = baseEntry / conversionRateBetweenBaseCurrencyAndDollars
                val conversionRateBetweenTargetCurrencyAndDollars =
                    rates.value?.firstOrNull { it.to == target.value }?.rate

                conversionRateBetweenTargetCurrencyAndDollars?.let {
                    targetAmount.postValue(
                        dollarValue * it
                    )
                }
            }
        }
    }

    //For calculating historic data, euro conversion rates are used to cross convert to get rates for other currencies
    private fun crossConvertEuro(
        historicRates: Map<String, Float>,
        base: String,
        target: String
    ): Float {
        val conversionRateBetweenBaseCurrencyAndEuro = historicRates[base]
        if (conversionRateBetweenBaseCurrencyAndEuro != null) {
            val euroValue = 1 / conversionRateBetweenBaseCurrencyAndEuro
            val conversionRateBetweenTargetCurrencyAndEuro =
                historicRates[target]

            conversionRateBetweenTargetCurrencyAndEuro?.let {
                return euroValue * it
            }
        }
        return 0F
    }

    //Handles request for 30 days historical data
    fun getHistoricalRatesThirty() {
        //Get 10 past days evenly
        val timelineDates = arrayListOf<String>()
        val dateNow = DateTime.nowLocal()
        for (i in 3..30 step 3) {
            val threeDaysBack = dateNow.minus(i.days)
            timelineDates.add(threeDaysBack.format(DateFormat.FORMAT_DATE))
        }

        viewModelScope.launch {
            getAPIKeyFromSharedPref()?.let {
                launch(Dispatchers.IO) {
                    val ratesHolder = mutableListOf<Pair<String, Float>>()
                    var failed = false
                    for (i in timelineDates) {
                        val getHistoricRates =
                            repository.getHistoricRate(date = i, base = "EUR", key = it)

                        if (getHistoricRates.isNotEmpty()) {
                            val dateFormat = DateFormat.FORMAT_DATE

                            val readableDate =
                                "${dateFormat.parse(i).dayOfMonth} ${dateFormat.parse(i).month.localShortName.capitalize()}"

                            val rate = crossConvertEuro(
                                getHistoricRates,
                                base.value ?: "USD",
                                target.value ?: "EUR"
                            )

                            ratesHolder.add(
                                Pair(
                                    readableDate,
                                    rate
                                )
                            )
                        } else {
                            errorMessage.postValue("Error, please check connection or add new API key and retry.")
                            failed = true
                        }
                    }
                    if(failed) historicRates.postValue(null) else historicRates.postValue(ratesHolder)
                }
            }
        }
    }

    //Handles request for 90 days historical data
    fun getHistoricalRatesNinety() {
        //Get 10 past days evenly
        val timelineDates = arrayListOf<String>()
        val dateNow = DateTime.nowLocal()
        for (i in 9..90 step 9) {
            val threeDaysBack = dateNow.minus(i.days)
            timelineDates.add(threeDaysBack.format(DateFormat.FORMAT_DATE))
        }

        viewModelScope.launch {
            getAPIKeyFromSharedPref()?.let {
                launch(Dispatchers.IO) {
                    val ratesHolder = mutableListOf<Pair<String, Float>>()
                    var failed = false
                    for (i in timelineDates) {
                        val getHistoricRates =
                            repository.getHistoricRate(date = i, base = "EUR", key = it)

                        if (getHistoricRates.isNotEmpty()) {
                            val dateFormat = DateFormat.FORMAT_DATE

                            val readableDate =
                                "${dateFormat.parse(i).dayOfMonth} ${dateFormat.parse(i).month.localShortName.capitalize()}"

                            val rate = crossConvertEuro(
                                getHistoricRates,
                                base.value ?: "USD",
                                target.value ?: "EUR"
                            )

                            ratesHolder.add(
                                Pair(
                                    readableDate,
                                    rate
                                )
                            )
                        } else {
                            errorMessage.postValue("Error, please check connection or add new API key and retry.")
                            failed = true
                        }
                    }
                    if(failed) historicRates.postValue(null) else historicRates.postValue(ratesHolder)
                }
            }
        }
    }

    /**
     * Get and save api key to shared preference
     */
    fun getAPIKeyFromSharedPref(): String? {
        val sharedPref =
            getApplication<Application>().getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        return sharedPref.getString("api-key", API_KEY)
    }

    fun saveAPIKeyToSharedPreference(key: String) {
        val sharedPref =
            getApplication<Application>().getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("api-key", key)
            apply()
        }
    }
}