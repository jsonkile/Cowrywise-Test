package com.golde.cowrywise.Network

import com.golde.cowrywise.Util.BASE_URL
import com.golde.cowrywise.Util.DateTimeUtil
import com.soywiz.klock.DateTime
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FixerApi {

    @GET("/latest")
    suspend fun getRatesAsync(@Query("access_key") access_key: String) : retrofit2.Response<String>

    @GET("{date}")
    suspend fun getHistoricalRatesAsync(@Path("date") date: String = DateTimeUtil.format(DateTime.now()), @Query("symbols") symbols : String, @Query("access_key") access_key: String) : retrofit2.Response<String>

    companion object {
        fun create(): FixerApi {

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()

            return retrofit.create(FixerApi::class.java)
        }
    }
}