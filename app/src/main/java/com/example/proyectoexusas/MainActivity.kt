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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectoexcusas.DocenteScreen
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
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginScreen(navController) }
                composable("excusa/{alumnoId}") { backStackEntry ->
                    val alumnoId = backStackEntry.arguments?.getString("alumnoId")?.toIntOrNull() ?: 0
                    ExcusaScreen(alumnoId, navController)
                }
                composable("docente/{docenteId}") { backStackEntry ->
                    val docenteId = backStackEntry.arguments?.getString("docenteId")?.toIntOrNull() ?: 0
                    DocenteScreen(docenteId, navController)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
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

        Text(
            "SISTEMA DE EXCUSAS UNICAH",
            style = MaterialTheme.typography.h6,
            color = Color(0xFF003366)
        )

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
                            val response: HttpResponse = client.post("http://192.168.100.3:3008/api/unicah/user/login") {
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

                            when (loginResponse.user.roleId) {
                                2 -> {
                                    val alumnoId = loginResponse.alumnoId ?: 0
                                    navController.navigate("excusa/$alumnoId")
                                }
                                3 -> {
                                    val docenteId = loginResponse.docenteId ?: 0
                                    navController.navigate("docente/$docenteId")
                                }
                                else -> Toast.makeText(context, "Rol no permitido", Toast.LENGTH_SHORT).show()
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
    val user: User,
    val alumnoId: Int? = null,
    val docenteId: Int? = null
)

@Serializable
data class User(
    val userId: Int,
    val pass: String,
    val roleId: Int
)