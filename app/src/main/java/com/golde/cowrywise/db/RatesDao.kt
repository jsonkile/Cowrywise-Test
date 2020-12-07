package com.golde.cowrywise.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.golde.cowrywise.models.Rate
import com.golde.cowrywise.models.Currency

@Dao
interface RatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun storeRate(rates: List<Rate>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun storeCurrency(symbols: List<Currency>)

    @Query("SELECT * FROM rates")
    fun getRates(): LiveData<List<Rate>>

    @Query("SELECT * FROM currencies")
    fun getCurrencies(): LiveData<List<Currency>>
}