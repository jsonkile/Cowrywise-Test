package com.golde.cowrywise.di

import androidx.room.Room
import com.golde.cowrywise.db.AppDatabase
import com.golde.cowrywise.network.FixerService
import com.golde.cowrywise.repos.RatesRepository
import com.golde.cowrywise.repos.RatesRepositoryImpl
import com.golde.cowrywise.ui.viewmodels.BaseViewModel
import com.google.gson.GsonBuilder
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    //DB instance
    single {
        Room.databaseBuilder(androidContext(),
            AppDatabase::class.java, "rates-database").build()
    }

    //DB Dao
    single { get<AppDatabase>().deliverRatesDao() }

    single {
        Retrofit.Builder()
            .baseUrl("http://data.fixer.io/api/")
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build().create(FixerService::class.java)
    }

    /**
     * Deliver a single instance of the Rates Repo
     */
    single<RatesRepository> { RatesRepositoryImpl(get(), get()) }

    //Rates ViewModel
    viewModel { BaseViewModel(get(), androidApplication()) }

}