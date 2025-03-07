package com.example.proyectoexusas

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ExcusaScreen() {
    var selectedReason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("Seleccionar archivo") }

    Scaffold(
        topBar = { CustomTopBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery),// Cambiar por el escudo de UNICAH
                contentDescription = "Escudo UNICAH",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "DANIEL WILSON JAVIER DIAZ",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            val reasons = listOf("Enfermedad", "Luto", "Viaje", "Otro")
            reasons.forEach { reason ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedReason = reason }
                        .padding(8.dp)
                ) {
                    Checkbox(
                        checked = selectedReason == reason,
                        onCheckedChange = { selectedReason = reason }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(reason)
                }
            }

            if (selectedReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe la inasistencia") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // Aquí luego agregaremos la lógica para adjuntar archivo
            }) {
                Text(fileName)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    println("Excusa enviada")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("ENVIAR", color = Color.White)
            }
        }
    }
}
