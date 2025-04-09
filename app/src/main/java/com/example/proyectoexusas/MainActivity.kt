@file:OptIn(ExperimentalMaterialApi::class)

package com.example.proyectoexusas

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectoexusas.ui.theme.ProyectoExusasTheme
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProyectoExusasTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }

                    composable("excusa/{alumnoId}") { backStackEntry ->
                        val alumnoId = backStackEntry.arguments?.getString("alumnoId") ?: ""
                        ExcusaScreen(alumnoId)
                    }

                    composable("docente/{docenteId}") { backStackEntry ->
                        val docenteId = backStackEntry.arguments?.getString("docenteId") ?: ""
                        DocenteScreen(docenteId)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember { HttpClient(CIO) }

    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://login.sec.unicah.net/imgs/NewLogo.png"),
            contentDescription = "Logo UNICAH",
            modifier = Modifier
                .height(100.dp)
                .padding(bottom = 16.dp)
        )

        Text("SISTEMA DE EXCUSAS UNICAH", style = MaterialTheme.typography.h6, color = Color(0xFF003366))

        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("Usuario") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                if (userId.isNotBlank() && password.isNotBlank()) {
                    scope.launch {
                        try {
                            val response: HttpResponse = client.post("http://192.168.1.7:3008/api/unicah/user/login") {
                                contentType(ContentType.Application.Json)
                                setBody("""{ "userId": "$userId", "pass": "$password" }""")
                            }

                            if (!response.status.isSuccess()) {
                                Toast.makeText(context, "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            val body = response.bodyAsText()
                            Log.d("API_RESPONSE", body)

                            val loginResponse = Json.decodeFromString<LoginResponse>(body)

                            when {
                                loginResponse.alumnoId != null -> {
                                    navController.navigate("excusa/${loginResponse.alumnoId}")
                                }
                                loginResponse.docenteId != null -> {
                                    navController.navigate("docente/${loginResponse.docenteId}")
                                }
                                else -> {
                                    Toast.makeText(context, "No se encontró rol válido", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("LOGIN_ERROR", e.stackTraceToString())
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF003366)),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text("Iniciar Sesión", color = Color.White)
        }
    }
}

@Serializable
data class LoginResponse(
    val message: String,
    val user: User,
    val alumnoId: String? = null,
    val docenteId: String? = null
)

@Serializable
data class User(
    val userId: String,
    val pass: String,
    val roleId: Int
)
