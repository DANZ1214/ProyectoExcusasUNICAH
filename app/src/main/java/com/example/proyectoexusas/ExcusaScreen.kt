package com.example.proyectoexusas

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcusaScreen() {
    var selectedReason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("Seleccionar archivo") }
    val reasonImages = mutableStateMapOf(
        "Enfermedad" to "https://i.postimg.cc/3J052qVw/image-removebg-preview-55.png",
        "Luto" to "https://i.postimg.cc/T1ZskRF3/image-removebg-preview-67.png",
        "Viaje" to "https://i.postimg.cc/vB2kV7v9/image-removebg-preview-66.png",
        "Otro" to "https://i.postimg.cc/5tnkh5TG/image-removebg-preview-68.png"
    )

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .border(2.dp, Color(0xFF1E50A2)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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

            Image(
                painter = rememberImagePainter("https://i.postimg.cc/NfcLn1tB/image-removebg-preview-65.png"),
                contentDescription = "Escudo UNICAH",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "JENRRI BIERA PISADO",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            val reasons = listOf("Enfermedad", "Luto", "Viaje", "Otro")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                reasons.forEach { reason ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Checkbox(
                            checked = selectedReason == reason,
                            onCheckedChange = {
                                if (selectedReason == reason) selectedReason = "" else selectedReason = reason
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color.Black, uncheckedColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Image(
                            painter = rememberImagePainter(reasonImages[reason] ?: ""),
                            contentDescription = reason,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(reason, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                }
            }

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

            Button(
                onClick = { /* Lógica para adjuntar archivo */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB22222))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberImagePainter("https://i.postimg.cc/zfVfh7CS/pdf.png"),
                        contentDescription = "Adjuntar PDF",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(fileName, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Lógica para enviar */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E50A2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ENVIAR", color = Color.White)
            }
        }
    }
}