package com.ralugan.raluganplus.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://query.wikidata.org/bigdata/namespace/wdq/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getWikidataApi(): WikidataApi {
        return retrofit.create(WikidataApi::class.java)
    }
}
