package br.edu.utfpr.calculatorappclient

data class RestRequest(
    val numA: Float,
    val numB: Float
)

data class RestResponse(
    val result: Float
)