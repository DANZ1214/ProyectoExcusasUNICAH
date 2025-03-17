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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import kotlinx.coroutines.launch

/**
 * ExcusaScreen: Pantalla para que el usuario seleccione una razón de excusa,
 * escriba una descripción y adjunte un archivo.
 */
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcusaScreen() {
    // Estado para almacenar la razón seleccionada.
    var selectedReason by remember { mutableStateOf("") }
    // Estado para almacenar la descripción de la excusa.
    var description by remember { mutableStateOf("") }
    // Estado para almacenar el nombre del archivo adjunto.
    var fileName by remember { mutableStateOf("Seleccionar archivo") }

    // Mapa de imágenes para cada razón de excusa.
    val reasonImages = mutableStateMapOf(
        "Enfermedad" to "https://i.postimg.cc/3J052qVw/image-removebg-preview-55.png",
        "Luto" to "https://i.postimg.cc/T1ZskRF3/image-removebg-preview-67.png",
        "Viaje" to "https://i.postimg.cc/vB2kV7v9/image-removebg-preview-66.png",
        "Otro" to "https://i.postimg.cc/5tnkh5TG/image-removebg-preview-68.png"
    )

    // CoroutineScope para manejar operaciones asíncronas.
    val coroutineScope = rememberCoroutineScope()
    // Estado para almacenar el mensaje de alerta.
    var alertMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.White // Color de fondo de la pantalla.
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Habilita el desplazamiento vertical.
                .border(2.dp, Color(0xFF1E50A2)), // Borde de la columna.
            horizontalAlignment = Alignment.CenterHorizontally // Alineación horizontal de los elementos.
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Barra superior personalizada.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E50A2))
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SISTEMA DE EXCUSAS UNICAH",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Imagen del escudo de UNICAH.
            Image(
                painter = rememberImagePainter("https://i.postimg.cc/NfcLn1tB/image-removebg-preview-65.png"),
                contentDescription = "Escudo UNICAH",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Texto de bienvenida.
            Text(
                text = "BIENVENIDO",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Título para la selección de razón.
            Text(
                text = "SELECCIONA UNA RAZÓN",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E50A2))
                    .padding(8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de razones de excusa.
            val reasons = listOf("Enfermedad", "Luto", "Viaje", "Otro")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                reasons.forEach { reason ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Botón de radio para cada razón.
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = Color.Black, unselectedColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Imagen correspondiente a la razón.
                        Image(
                            painter = rememberImagePainter(reasonImages[reason] ?: ""),
                            contentDescription = reason,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Texto de la razón.
                        Text(reason, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                }
            }

            // Campo de texto para la descripción de la excusa.
            if (selectedReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe la inasistencia", color = Color.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF1E50A2),
                        unfocusedBorderColor = Color(0xFF1E50A2)
                    ),
                    textStyle = TextStyle(color = Color.Black)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para seleccionar un archivo.
            Button(
                onClick = { fileName = "Archivo Seleccionado.pdf" },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB22222))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Imagen de PDF.
                    Image(
                        painter = rememberImagePainter("https://i.postimg.cc/zfVfh7CS/pdf.png"),
                        contentDescription = "Adjuntar PDF",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Nombre del archivo.
                    Text(fileName, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para enviar la excusa.
            Button(
                onClick = {
                    coroutineScope.launch {
                        enviarExcusa(selectedReason, description, fileName, { message ->
                            alertMessage = message
                        })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E50A2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ENVIAR", color = Color.White)
            }
        }
    }

    // Mostrar alerta si hay un mensaje de alerta.
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

/**
 * enviarExcusa: Función para simular el envío de una excusa.
 *
 * @param razon Razón de la excusa.
 * @param descripcion Descripción de la excusa.
 * @param archivo Nombre del archivo adjunto.
 * @param onMessage Función lambda para mostrar un mensaje al usuario.
 */
fun enviarExcusa(razon: String, descripcion: String, archivo: String, onMessage: (String) -> Unit) {
    if (razon.isEmpty() || descripcion.isEmpty()) {
        onMessage("Por favor, selecciona una razón y escribe una descripción")
        return
    }

    // Simulación de envío
    onMessage("Excusa enviada con éxito")
}