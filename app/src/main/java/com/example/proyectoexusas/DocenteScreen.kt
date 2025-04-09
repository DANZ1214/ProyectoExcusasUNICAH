package com.example.proyectoexcusas    //dOCENTEsCREEN.KT

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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
fun DocenteScreen(docenteId: Int, navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember { HttpClient(CIO) }

    var clases by remember { mutableStateOf(listOf<Clase>()) }
    var excusas by remember { mutableStateOf(listOf<Excusa>()) }
    var selectedClases by remember { mutableStateOf(listOf<Int>()) }

    LaunchedEffect(Unit) {
        try {
            val resClases = client.get("http://192.168.100.3:3008/api/unicah/matriculaAlumno/getClasesDocente/$docenteId")
            clases = Json.decodeFromString(resClases.bodyAsText())

            val resExcusas = client.get("http://192.168.100.3:3008/api/unicah/excusa/getExcusasDocente/$docenteId")
            excusas = Json.decodeFromString(resExcusas.bodyAsText())
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
        }
    }

    val filtered = if (selectedClases.isEmpty()) excusas else {
        excusas.filter { excusa ->
            excusa.clases.any { selectedClases.contains(it.id_clase) }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Image(
            painter = rememberAsyncImagePainter("https://login.sec.unicah.net/imgs/NewLogo.png"),
            contentDescription = null,
            modifier = Modifier.height(100.dp)
        )

        Text("UNICAH - VISTA DOCENTE", style = MaterialTheme.typography.h6, color = Color(0xFF003366))

        Text("Clases asignadas:", modifier = Modifier.padding(top = 8.dp))
        clases.forEach { clase ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedClases = if (selectedClases.contains(clase.id_clase))
                            selectedClases - clase.id_clase
                        else
                            selectedClases + clase.id_clase
                    }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = selectedClases.contains(clase.id_clase),
                    onCheckedChange = null
                )
                Text("${clase.nombre_clase} (${clase.id_clase})")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(filtered) { excusa ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Alumno: ${excusa.alumno.nombre}")
                        Text("Motivo: ${excusa.razon}")
                        Text("Descripción: ${excusa.descripcion}")
                        Text("Fecha: ${excusa.fecha_solicitud}")
                        Text("Clase(s): ${excusa.clases.joinToString { it.nombre_clase }}")

                        Text(
                            "Archivo: ${excusa.archivo ?: "No adjunto"}",
                            color = Color.Blue,
                            modifier = Modifier.clickable {
                                excusa.archivo?.let {
                                    val uri = Uri.parse("http://192.168.100.3:3008/uploads/$it")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                }
                            }
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Estado: ")
                            when (excusa.estado) {
                                "Pendiente" -> {
                                    Text("Pendiente", color = Color.Yellow)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Button(onClick = {
                                        actualizarEstado(scope, client, excusa.id_excusa, "Aprobado", context) {
                                            excusas = excusas.map {
                                                if (it.id_excusa == excusa.id_excusa) it.copy(estado = "Aprobado") else it
                                            }
                                        }
                                    }) { Text("Aprobar") }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = {
                                        actualizarEstado(scope, client, excusa.id_excusa, "Rechazado", context) {
                                            excusas = excusas.map {
                                                if (it.id_excusa == excusa.id_excusa) it.copy(estado = "Rechazado") else it
                                            }
                                        }
                                    }) { Text("Rechazar") }
                                }

                                "Aprobado" -> Text("Aprobado", color = Color.Green)
                                "Rechazado" -> Text("Rechazado", color = Color.Red)
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
            val response = client.put("http://192.168.100.3:3008/api/unicah/excusa/updateExcusa") {
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

@Serializable
data class Clase(
    val id_clase: Int,
    val nombre_clase: String
)
