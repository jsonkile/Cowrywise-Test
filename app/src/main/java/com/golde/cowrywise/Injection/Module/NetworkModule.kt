package com.golde.cowrywise.Injection.Module

import com.golde.cowrywise.Network.FixerApi
import com.golde.cowrywise.Util.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.Reusable
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Module which provides all required dependencies about network
 */
@Module
// Safe here as we are dealing with a Dagger 2 module
@Suppress("unused")
object NetworkModule {
    /**
     * Provides the Fixer service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the Fixer service implementation.
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun provideFixerApi(retrofit: Retrofit): FixerApi {
        return retrofit.create(FixerApi::class.java)
    }

    /**
     * Provides the Retrofit object.
     * @return the Retrofit object
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun provideRetrofitInterface(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
}