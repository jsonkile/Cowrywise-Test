package com.golde.cowrywise.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.golde.cowrywise.models.Rate
import com.golde.cowrywise.models.Currency

@Database(entities = [Rate::class, Currency::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deliverRatesDao() : RatesDao
}