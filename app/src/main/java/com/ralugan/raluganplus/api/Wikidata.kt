package com.ralugan.raluganplus.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface WikidataApi {
    @GET("sparql")
    fun getDisneyPlusInfo(@Query("query") query: String, @Query("format") format: String): Call<ResponseBody>
}
