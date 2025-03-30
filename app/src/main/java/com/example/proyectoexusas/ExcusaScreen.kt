package com.example.proyectoexusas

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

val client = HttpClient() // Importante: tenelo global o en otro archivo

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcusaScreen(
    onSelectFile: () -> Unit,
    fileName: String
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

    Scaffold(containerColor = Color.White) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .border(2.dp, Color(0xFF1E50A2))
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SISTEMA DE EXCUSAS UNICAH",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E50A2),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = rememberImagePainter("https://i.postimg.cc/NfcLn1tB/image-removebg-preview-65.png"),
                    contentDescription = "Escudo UNICAH",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("BIENVENIDO", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD6E4F0))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "SELECCIONA UNA RAZÓN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E50A2),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val reasons = listOf("Enfermedad", "Luto", "Viaje", "Otro")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    reasons.forEach { reason ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.Black,
                                    unselectedColor = Color.Black
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Image(
                                painter = rememberImagePainter(reasonImages[reason]),
                                contentDescription = reason,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(reason, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        }
                    }
                }

                if (selectedReason.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Describe la inasistencia:",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.Black),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF1E50A2),
                            unfocusedBorderColor = Color(0xFF1E50A2)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Seleccionar archivo (opcional):",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onSelectFile() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB22222))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberImagePainter("https://i.postimg.cc/zfVfh7CS/pdf.png"),
                            contentDescription = "Adjuntar PDF",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(fileName.ifEmpty { "Seleccionar archivo" }, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            enviarExcusaReal(
                                razon = selectedReason,
                                descripcion = description,
                                archivo = fileName,
                                alumnoId = 1 // Aquí ponés el ID real del alumno autenticado
                            ) { message ->
                                alertMessage = message
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E50A2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ENVIAR", color = Color.White)
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
                Button(onClick = { alertMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}
