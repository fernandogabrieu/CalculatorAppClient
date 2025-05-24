package br.edu.utfpr.calculatorappclient

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RestCalculatorService {
    @POST("calculator/add")
    fun add(@Body request: RestRequest): Call<RestResponse>

    @POST("calculator/subtract")
    fun subtract(@Body request: RestRequest): Call<RestResponse>
}