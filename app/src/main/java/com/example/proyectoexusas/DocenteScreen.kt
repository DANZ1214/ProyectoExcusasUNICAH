package com.example.proyectoexcusas

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Composable
fun DocenteScreen(docenteId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember { HttpClient(CIO) }

    var clases by remember { mutableStateOf(listOf<Clase>()) }
    var excusas by remember { mutableStateOf(listOf<Excusa>()) }
    var selectedClases by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        try {
            val json = Json { ignoreUnknownKeys = true }

            val resClases = client.get("http://192.168.1.7:3008/api/unicah/matriculaAlumno/getClasesDocente/$docenteId")
            clases = json.decodeFromString(resClases.bodyAsText())

            val resExcusas = client.get("http://192.168.1.7:3008/api/unicah/excusa/getExcusasDocente/$docenteId")
            excusas = json.decodeFromString(resExcusas.bodyAsText())

        } catch (e: Exception) {
            Toast.makeText(context, "Error cargando datos: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    val filtered = if (selectedClases.isEmpty()) excusas else {
        excusas.filter { it.clases.any { clase -> selectedClases.contains(clase.id_clase) } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://login.sec.unicah.net/imgs/NewLogo.png"),
            contentDescription = null,
            modifier = Modifier
                .height(100.dp)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            "UNICAH - VISTA DOCENTE",
            style = MaterialTheme.typography.h6,
            color = Color(0xFF003366),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text("Clases Asignadas:", style = MaterialTheme.typography.subtitle1)

        if (clases.isEmpty()) {
            Text("No se encontraron clases", color = Color.Red)
        } else {
            clases.forEach { clase ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable {
                            selectedClases = if (selectedClases.contains(clase.id_clase))
                                selectedClases - clase.id_clase
                            else
                                selectedClases + clase.id_clase
                        }
                ) {
                    Checkbox(
                        checked = selectedClases.contains(clase.id_clase),
                        onCheckedChange = null
                    )
                    Text("${clase.id_clase} - ${clase.nombre_clase}")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Text("No hay excusas para mostrar", color = Color.Gray)
        } else {
            Text("Excusas Recibidas:", style = MaterialTheme.typography.subtitle1)

            filtered.forEach { excusa ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Alumno: ${excusa.alumno.nombre}")
                        Text("Motivo: ${excusa.razon}")
                        Text("Descripción: ${excusa.descripcion}")
                        Text("Fecha: ${excusa.fecha_solicitud}")
                        Text("Clase(s): ${excusa.clases.joinToString { it.nombre_clase }}")

                        Log.d("ARCHIVO_ADJUNTO", "Archivo: ${excusa.archivo}")

                        if (!excusa.archivo.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        val uri = Uri.parse("http://192.168.1.7:3008/uploads/${excusa.archivo}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    }
                                    .padding(top = 6.dp)
                            ) {
                                Icon(
                                    painter = rememberAsyncImagePainter("https://cdn-icons-png.flaticon.com/512/337/337946.png"),
                                    contentDescription = "Archivo adjunto",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ver archivo adjunto",
                                    color = Color.Blue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Estado: ${excusa.estado}", color = when (excusa.estado) {
                                "Aprobado" -> Color.Green
                                "Rechazado" -> Color.Red
                                else -> Color.Yellow
                            })

                            if (excusa.estado == "Pendiente") {
                                Row {
                                    Button(
                                        onClick = {
                                            actualizarEstado(scope, client, excusa.id_excusa, "Aprobado", context) {
                                                excusas = excusas.map {
                                                    if (it.id_excusa == excusa.id_excusa) it.copy(estado = "Aprobado") else it
                                                }
                                            }
                                        },
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("Aprobar")
                                    }

                                    Button(
                                        onClick = {
                                            actualizarEstado(scope, client, excusa.id_excusa, "Rechazado", context) {
                                                excusas = excusas.map {
                                                    if (it.id_excusa == excusa.id_excusa) it.copy(estado = "Rechazado") else it
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Rechazar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun actualizarEstado(
    scope: CoroutineScope,
    client: HttpClient,
    id_excusa: Int,
    estado: String,
    context: Context,
    onSuccess: () -> Unit
) {
    scope.launch {
        try {
            val response = client.put("http://192.168.1.7:3008/api/unicah/excusa/updateExcusa") {
                contentType(ContentType.Application.Json)
                setBody("""{ "id_excusa": $id_excusa, "estado": "$estado" }""")
            }

            if (response.status == HttpStatusCode.OK) {
                Toast.makeText(context, "Estado actualizado a $estado", Toast.LENGTH_SHORT).show()
                onSuccess()
            } else {
                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Fallo al conectar con el servidor", Toast.LENGTH_SHORT).show()
        }
    }
}

@Serializable
data class Excusa(
    val id_excusa: Int,
    val razon: String,
    val descripcion: String,
    val archivo: String? = null,
    val fecha_solicitud: String,
    val estado: String,
    val alumno: Alumno,
    val clases: List<Clase>
)

@Serializable
data class Alumno(
    val alumnoId: Int,
    val nombre: String
)


