package com.golde.cowries.Network

import com.golde.cowries.Util.BASE_URL
import com.golde.cowries.Util.DateTimeUtil
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FixerApi {

    @GET("/latest")
    suspend fun getRatesAsync(@Query("access_key") access_key: String = "96cff73c29dbdb708d3ca08f9e9b14d7") : retrofit2.Response<String>

    @GET("{date}")
    suspend fun getHistoricalRatesAsync(@Path("date") date: String = DateTimeUtil.format(DateTime.now()), @Query("symbols") symbols : String, @Query("access_key") access_key: String = "96cff73c29dbdb708d3ca08f9e9b14d7") : retrofit2.Response<String>

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