package com.golde.cowrywise.network

import com.golde.cowrywise.models.ErrorResponse
import com.golde.cowrywise.models.HistoricalSymbol
import com.golde.cowrywise.models.RemoteRate
import com.golde.cowrywise.models.RemoteSymbol
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FixerService {

    //Get latest rates
    @GET("/latest")
    suspend fun getRates(@Query("access_key") key: String) : NetworkResponse<RemoteRate, ErrorResponse>

    //Get currency symbols
    @GET("/symbols")
    suspend fun getSymbols(@Query("access_key") key: String) : NetworkResponse<RemoteSymbol, ErrorResponse>

    //Get historic rates
    @GET("{date}")
    suspend fun getHistoricalRate(@Path("date") date: String, @Query("base") base: String, @Query("access_key") access_key: String) : NetworkResponse<HistoricalSymbol, ErrorResponse>

}