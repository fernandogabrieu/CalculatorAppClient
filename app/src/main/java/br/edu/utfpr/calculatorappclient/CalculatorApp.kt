package br.edu.utfpr.calculatorappclient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorApp() {
    val display = remember { mutableStateOf("0") }
    val firstOperand = remember { mutableStateOf("") }
    val operation = remember { mutableStateOf("") }
    val useSoap = remember { mutableStateOf(false) }
    val justCalculated = remember { mutableStateOf(false) }
    val rawResponse = remember { mutableStateOf("") } // Nova variável para mostrar o retorno bruto da API

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // mostra a expressão inteira
        val expression = remember(display.value, firstOperand.value, operation.value) {
            buildString {
                if (firstOperand.value.isNotEmpty()) append(firstOperand.value)
                if (operation.value.isNotEmpty()) append(" ${operation.value} ")
                if (display.value != "0" || (firstOperand.value.isEmpty() && operation.value.isEmpty())) append(display.value)
            }
        }

        Text(
            text = expression,
            fontSize = 32.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(16.dp),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(8.dp))

        // switch com label dinâmico
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = if (useSoap.value) "Usando SOAP" else "Usando REST")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = useSoap.value,
                onCheckedChange = { useSoap.value = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // buttons
        val buttons = listOf(
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
            listOf("0", ".", "C"),
            listOf("+", "-", "=")
        )

        for (row in buttons) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (label in row) {
                    Button(
                        onClick = {
                            when (label) {
                                in "0".."9" -> {
                                    if (justCalculated.value) {
                                        display.value = label
                                        justCalculated.value = false
                                    } else {
                                        if (display.value == "0") {
                                            display.value = label
                                        } else {
                                            display.value += label
                                        }
                                    }
                                }
                                "." -> {
                                    if (!display.value.contains(".")) {
                                        display.value += "."
                                    }
                                }
                                "C" -> {
                                    display.value = "0"
                                    firstOperand.value = ""
                                    operation.value = ""
                                    rawResponse.value = "" // limpa response exibido no quadro também
                                    justCalculated.value = false
                                }
                                "+", "-" -> {
                                    if (firstOperand.value.isEmpty()) {
                                        firstOperand.value = display.value
                                        operation.value = label
                                        display.value = "0"
                                    } else {
                                        operation.value = label
                                    }
                                    justCalculated.value = false
                                }
                                "=" -> {
                                    if (firstOperand.value.isNotEmpty() && operation.value.isNotEmpty()) {
                                        val a = firstOperand.value.toFloatOrNull() ?: 0f
                                        val b = display.value.toFloatOrNull() ?: 0f
                                        val op = when (operation.value) {
                                            "+" -> "Add"
                                            "-" -> "Subtract"
                                            else -> ""
                                        }
                                        if (op.isNotEmpty()) {
                                            if (useSoap.value) {
                                                // chamada assíncrona para SOAP
                                                callSoapService(op, a, b) { result, raw ->
                                                    display.value = result
                                                    rawResponse.value = raw
                                                    firstOperand.value = ""
                                                    operation.value = ""
                                                    justCalculated.value = true
                                                }
                                            } else {
                                                // chamada assíncrona para REST
                                                callRestService(op, a, b) { result, raw ->
                                                    display.value = result
                                                    rawResponse.value = raw
                                                    firstOperand.value = ""
                                                    operation.value = ""
                                                    justCalculated.value = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(text = label, fontSize = 24.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //quadro para exibir a response da API
        Text(
            text = "Response da API:",
            style = MaterialTheme.typography.labelLarge
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .border(1.dp, Color.Gray)
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = rawResponse.value)
        }
    }
}
