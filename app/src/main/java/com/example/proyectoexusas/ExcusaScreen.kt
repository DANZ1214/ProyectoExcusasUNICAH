package com.example.proyectoexcusas

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun ExcusaScreen(alumnoId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember { HttpClient(CIO) }

    val razones = listOf("Enfermedad", "Luto", "Viaje", "Otro")
    val imagenes = mapOf(
        "Enfermedad" to "https://i.postimg.cc/3J052qVw/image-removebg-preview-55.png",
        "Luto" to "https://i.postimg.cc/T1ZskRF3/image-removebg-preview-67.png",
        "Viaje" to "https://i.postimg.cc/vB2kV7v9/image-removebg-preview-66.png",
        "Otro" to "https://i.postimg.cc/5tnkh5TG/image-removebg-preview-68.png"
    )

    var selectedRazon by remember { mutableStateOf<String?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var selectedClases by remember { mutableStateOf(listOf<String>()) }
    var clases by remember { mutableStateOf<List<Clase>>(emptyList()) }
    var archivoUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        archivoUri = uri
    }

    LaunchedEffect(Unit) {
        try {
            val response = client.get("http://192.168.1.7:3008/api/unicah/matriculaAlumno/getClasesAlumno/$alumnoId")
            val json = Json { ignoreUnknownKeys = true }
            clases = json.decodeFromString(response.bodyAsText())
        } catch (e: Exception) {
            Log.e("EXCUSA_ERROR", e.stackTraceToString())
            Toast.makeText(context, "Error al cargar clases: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://login.sec.unicah.net/imgs/NewLogo.png"),
            contentDescription = null,
            modifier = Modifier.height(100.dp),
            contentScale = ContentScale.Fit
        )

        Text("SISTEMA DE EXCUSAS UNICAH", color = Color(0xFF003366), style = MaterialTheme.typography.h6)
        Text("BIENVENIDO", style = MaterialTheme.typography.h6, modifier = Modifier.padding(bottom = 16.dp))

        Text("Selecciona una razón:", style = MaterialTheme.typography.body1)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            razones.forEach { razon ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { selectedRazon = razon }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imagenes[razon]),
                        contentDescription = razon,
                        modifier = Modifier.size(60.dp)
                    )
                    RadioButton(selected = selectedRazon == razon, onClick = { selectedRazon = razon })
                    Text(razon, style = MaterialTheme.typography.caption)
                }
            }
        }

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Describe la inasistencia") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Text("Selecciona las clases:", style = MaterialTheme.typography.body1)
        clases.forEach { clase ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .toggleable(
                        value = selectedClases.contains(clase.id_clase),
                        onValueChange = {
                            selectedClases = if (it)
                                selectedClases + clase.id_clase
                            else
                                selectedClases - clase.id_clase
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = selectedClases.contains(clase.id_clase), onCheckedChange = null)
                Text("${clase.nombre_clase} (${clase.id_clase})")
            }
        }

        Button(onClick = { filePickerLauncher.launch("*/*") }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Seleccionar archivo")
        }

        archivoUri?.let {
            Text("Archivo seleccionado: ${it.lastPathSegment}", style = MaterialTheme.typography.caption)
        }

        Button(
            onClick = {
                if (selectedRazon == null || descripcion.isBlank() || selectedClases.isEmpty()) {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                scope.launch {
                    try {
                        val contentResolver = context.contentResolver
                        val inputStream = archivoUri?.let { contentResolver.openInputStream(it) }
                        val fileBytes = inputStream?.readBytes()
                        val fileName = archivoUri?.lastPathSegment ?: "archivo.pdf"

                        val formData = MultiPartFormDataContent(
                            formData {
                                append("alumnoId", alumnoId)
                                append("razon", selectedRazon!!)
                                append("descripcion", descripcion)
                                append("clases", Json.encodeToString(selectedClases))

                                if (fileBytes != null) {
                                    append("archivo", fileBytes, Headers.build {
                                        append(HttpHeaders.ContentDisposition, "form-data; name=\"archivo\"; filename=\"$fileName\"")
                                        append(HttpHeaders.ContentType, "application/octet-stream")
                                    })
                                }
                            }
                        )

                        val response = client.post("http://192.168.1.7:3008/api/unicah/excusa/insertExcusa") {
                            setBody(formData)
                        }

                        if (response.status == HttpStatusCode.Created) {
                            Toast.makeText(context, "Excusa enviada correctamente", Toast.LENGTH_LONG).show()
                            selectedRazon = null
                            descripcion = ""
                            selectedClases = emptyList()
                            archivoUri = null
                        } else {
                            Toast.makeText(context, "Error al enviar excusa", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Log.e("ENVIO_ERROR", e.stackTraceToString())
                        Toast.makeText(context, "Fallo al enviar excusa", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF003366)),
            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
        ) {
            Text("Enviar Excusa", color = Color.White)
        }
    }
}

@Serializable
data class Clase(
    val id_clase: String,
    val nombre_clase: String
)