package br.edu.utfpr.calculatorappclient

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call as RetrofitCall
import retrofit2.Callback as RetrofitCallback
import retrofit2.Response as RetrofitResponse
import java.io.IOException

// Função REST
fun callRestService(operation: String, a: Float, b: Float, callback: (String, String) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://soap-rest-demo-00e3708b2060.herokuapp.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(RestCalculatorService::class.java)
    val request = RestRequest(a, b)

    val call: RetrofitCall<RestResponse> = if (operation == "Add") {
        service.add(request)
    } else {
        service.subtract(request)
    }

    call.enqueue(object : RetrofitCallback<RestResponse> {
        override fun onResponse(
            call: RetrofitCall<RestResponse>,
            response: RetrofitResponse<RestResponse>
        ) {
            if (response.isSuccessful) {
                val result = response.body()?.result?.toString() ?: "Erro"
                val raw = response.body().toString()
                callback(result, raw)
            } else
                callback("Erro", "Erro REST: ${response.code()}")
        }

        override fun onFailure(call: RetrofitCall<RestResponse>, t: Throwable) {
            callback("Erro", "Erro REST: ${t.message}")
        }
    })
}

// Função SOAP
fun callSoapService(operation: String, a: Float, b: Float, callback: (String, String) -> Unit) {
    val client = OkHttpClient()

    val xml = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                       xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body xmlns:tns="http://example.com/calculator">
            <tns:$operation>
              <intA xsi:type="xsd:float">$a</intA>
              <intB xsi:type="xsd:float">$b</intB>
            </tns:$operation>
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

    val body = xml.toRequestBody("text/xml; charset=utf-8".toMediaType())

    val request = Request.Builder()
        .url("https://soap-rest-demo-00e3708b2060.herokuapp.com/calculator")
        .addHeader("Content-Type", "text/xml;charset=UTF-8")
        .addHeader("SOAPAction", "\"$operation\"")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback("Erro", "Erro SOAP: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            val raw = response.body?.string() ?: "Sem resposta"
            if (response.isSuccessful) {
                // parse do XML para pegar só o número
                val regex = """<tns:result>(.*?)</tns:result>""".toRegex()
                val match = regex.find(raw)
                val result = match?.groups?.get(1)?.value ?: "Erro"
                callback(result, raw)
            } else
                callback("Erro", "Erro SOAP: ${response.code}")
        }
    })
}

