package com.golde.cowrywise.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.golde.cowrywise.getOrAwaitValue
import com.golde.cowrywise.models.Currency
import com.golde.cowrywise.models.Rate
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RatesDaoTest {

    //Run code sequentially
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var dao: RatesDao

    //Create new db instance before every test
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.deliverRatesDao()
    }

    //Close db instance after every test
    @After
    fun teardown(){
        database.close()
    }

    //Test 'insert rates' case
    @Test
    fun insertRates() = runBlockingTest {
        val rateOne = Rate("EUR", 6.78F)
        val rateTwo = Rate("JPY", 6.68F)
        val rates = listOf(rateOne, rateTwo)
        dao.storeRate(rates)

        val ratesFromDB = dao.getRates().getOrAwaitValue()
        assertThat(ratesFromDB).contains(rateOne)
    }

    //test 'insert currencies' case
    @Test
    fun insertCurrencies() = runBlockingTest {
        val curr1 = Currency("EUR", "Europe")
        val curr2 = Currency("JPY", "Japan")
        val currencies = listOf(curr1, curr2)
        dao.storeCurrency(currencies)

        val currenciesFromDB = dao.getCurrencies().getOrAwaitValue()
        assertThat(currenciesFromDB).contains(curr2)
    }


    @Test
    fun getRates() = runBlockingTest {
        val rateOne = Rate("EUR", 6.78F)
        val rateTwo = Rate("JPY", 6.68F)
        val rates = listOf(rateOne, rateTwo)
        dao.storeRate(rates)

        val ratesFromDB = dao.getRates().getOrAwaitValue()
        assertThat(ratesFromDB).hasSize(2)
    }


    @Test
    fun getCurrencies() = runBlockingTest {
        val curr1 = Currency("EUR", "Europe")
        val curr2 = Currency("JPY", "Japan")
        val currencies = listOf(curr1, curr2)
        dao.storeCurrency(currencies)

        val currenciesFromDB = dao.getCurrencies().getOrAwaitValue()
        assertThat(currenciesFromDB).hasSize(2)
    }

}