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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

val client = HttpClient()

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcusaScreen(
    onSelectFile: () -> Unit,
    fileName: String,
    alumnoId: String
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "SISTEMA DE EXCUSAS UNICAH",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF0D6EFD),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = rememberImagePainter("https://i.postimg.cc/NfcLn1tB/image-removebg-preview-65.png"),
            contentDescription = "Escudo UNICAH",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("SELECCIONA UNA RAZÓN", fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            reasonImages.keys.forEach { reason ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = selectedReason == reason,
                        onClick = { selectedReason = reason },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0D6EFD))
                    )
                    Image(
                        painter = rememberImagePainter(reasonImages[reason]),
                        contentDescription = reason,
                        modifier = Modifier.size(50.dp)
                    )
                    Text(reason, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Seleccionar archivo (PDF, JPG o PNG):", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { onSelectFile() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C757D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberImagePainter("https://i.postimg.cc/zfVfh7CS/pdf.png"),
                    contentDescription = "Adjuntar",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (fileName.isNotEmpty()) fileName else "Seleccionar archivo", color = Color.White)
            }
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D6EFD)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("ENVIAR", color = Color.White)
        }
    }

    alertMessage?.let {
        AlertDialog(
            onDismissRequest = { alertMessage = null },
            title = { Text("Mensaje") },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = { alertMessage = null }) {
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
    alumnoId: String,
    onMessage: (String) -> Unit
) {
    try {
        val apiUrl = "http://192.168.1.7:3008/api/unicah/excusa/insertExcusa"

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

        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val message = json["message"]?.jsonPrimitive?.content ?: "Excusa enviada con éxito"
        onMessage(message)
    } catch (e: Exception) {
        onMessage("Error de conexión: ${e.localizedMessage}")
    }
}
