package com.example.proyectoexusas

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import kotlinx.serialization.json.Json

val client = HttpClient()

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcusaScreen(
    onSelectFile: () -> Unit,
    fileName: String,
    alumnoId: Int
) {
    var selectedReason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var alertMessage by remember { mutableStateOf<String?>(null) }

    val reasonImages = mapOf(
        "Enfermedad" to "https://i.postimg.cc/3J052qVw/image-removebg-preview-55.png",
        "Luto" to "https://i.postimg.cc/T1ZskRF3/image-removebg-preview-67.png",
        "Viaje" to "https://i.postimg.cc/vB2kV7v9/image-removebg-preview-66.png",
        "Otro" to "https://i.postimg.cc/5tnkh5TG/image-removebg-preview-68.png"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.Center)
        ) {
            // Contenedor principal con sombra similar a Bootstrap
            Box(
                modifier = Modifier
                    .shadow(4.dp, shape = RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SISTEMA DE EXCUSAS UNICAH",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF0D6EFD),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        painter = rememberImagePainter("https://i.postimg.cc/NfcLn1tB/image-removebg-preview-65.png"),
                        contentDescription = "Escudo UNICAH",
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "BIENVENIDO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Alerta similar a Bootstrap
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFCFE2FF))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "SELECCIONA UNA RAZÓN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF084298),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Opciones de razón
                    val reasons = listOf("Enfermedad", "Luto", "Viaje", "Otro")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        reasons.forEach { reason ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { selectedReason = reason }
                            ) {
                                // Radio button personalizado
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(
                                            2.dp,
                                            if (selectedReason == reason) Color(0xFF0D6EFD) else Color.Gray,
                                            CircleShape
                                        )
                                        .clip(CircleShape)
                                ) {
                                    if (selectedReason == reason) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(Color(0xFF0D6EFD), CircleShape)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Image(
                                    painter = rememberImagePainter(reasonImages[reason]),
                                    contentDescription = reason,
                                    modifier = Modifier.size(50.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    reason,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    if (selectedReason.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Describe la inasistencia:",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            textStyle = TextStyle(color = Color.Black),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFF0D6EFD),
                                unfocusedBorderColor = Color(0xFF0D6EFD)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Seleccionar archivo (opcional):",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onSelectFile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C757D)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (fileName.isNotEmpty()) fileName else "Seleccionar archivo",
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (selectedReason.isEmpty()) {
                                alertMessage = "Por favor, selecciona una razón"
                                return@Button
                            }
                            if (description.isEmpty()) {
                                alertMessage = "Por favor, escribe una descripción"
                                return@Button
                            }

                            coroutineScope.launch {
                                enviarExcusaReal(
                                    razon = selectedReason,
                                    descripcion = description,
                                    archivo = fileName,
                                    alumnoId = alumnoId
                                ) {
                                    alertMessage = it
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D6EFD)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("ENVIAR", color = Color.White)
                    }
                }
            }
        }
    }

    if (alertMessage != null) {
        AlertDialog(
            onDismissRequest = { alertMessage = null },
            title = { Text("Mensaje") },
            text = { Text(alertMessage!!) },
            confirmButton = {
                Button(
                    onClick = { alertMessage = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D6EFD)
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }
}

suspend fun enviarExcusaReal(
    razon: String,
    descripcion: String,
    archivo: String,
    alumnoId: Int,
    onMessage: (String) -> Unit
) {
    try {
        val apiUrl = "http://192.168.100.3:3008/api/unicah/excusa/insertExcusa"

        val excuseData = buildJsonObject {
            put("alumnoId", alumnoId)
            put("razon", razon)
            put("descripcion", descripcion)
            if (archivo.isNotEmpty()) {
                put("archivo", archivo)
            }
        }

        val response: HttpResponse = client.post(apiUrl) {
            contentType(ContentType.Application.Json)
            setBody(excuseData.toString())
        }

        if (response.status.isSuccess()) {
            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            val message = json["message"]?.jsonPrimitive?.content ?: "Excusa enviada con éxito"
            onMessage(message)
        } else {
            val errorBody = response.bodyAsText()
            val json = Json.parseToJsonElement(errorBody).jsonObject
            val errorMsg = json["message"]?.jsonPrimitive?.content ?: "Error al enviar la excusa"
            onMessage(errorMsg)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onMessage("Error de conexión con el servidor: ${e.localizedMessage}")
    }
}