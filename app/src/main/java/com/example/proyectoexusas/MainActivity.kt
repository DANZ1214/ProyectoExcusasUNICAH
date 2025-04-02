package com.example.proyectoexusas

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.proyectoexusas.ui.theme.ProyectoExusasTheme
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

class MainActivity : ComponentActivity() {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var selectedFileUri by mutableStateOf<Uri?>(null)
    private var fileName by mutableStateOf("")

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedFileUri = uri
        fileName = uri?.lastPathSegment ?: "Archivo seleccionado"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProyectoExusasTheme {
                val navController = rememberNavController()

                Scaffold(
                    topBar = { CustomTopBar() },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginClick = { usuario, contrasena ->
                                    login(usuario, contrasena, navController)
                                }
                            )
                        }
                        composable("excusa/{alumnoId}") { backStackEntry ->
                            val alumnoId = backStackEntry.arguments?.getString("alumnoId")?.toIntOrNull() ?: 0
                            ExcusaScreen(
                                onSelectFile = { getContent.launch("*/*") },
                                fileName = fileName,
                                alumnoId = alumnoId
                            )
                        }
                    }
                }
            }
        }
    }

    private fun login(usuario: String, contrasena: String, navController: NavController) {
        scope.launch {
            try {
                val apiUrl = "http://192.168.100.3:3008/api/unicah/user/login"

                val response = client.post(apiUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("userId" to usuario, "pass" to contrasena))
                }

                val body = response.bodyAsText()
                val json = Json.parseToJsonElement(body).jsonObject
                val message = json["message"]?.jsonPrimitive?.content ?: "Error desconocido"

                if (message == "Login correcto") {
                    val alumnoId = json["alumnoId"]?.jsonPrimitive?.content?.toIntOrNull()

                    if (alumnoId != null) {
                        navController.navigate("excusa/$alumnoId")
                        errorMsg = ""
                    } else {
                        errorMsg = "Error: No se recibió ID de alumno"
                    }
                } else {
                    errorMsg = message
                }

            } catch (e: Exception) {
                errorMsg = "Error de conexión: ${e.message}"
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar() {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("EXCUSA", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E50A2))
    )
}

var errorMsg by mutableStateOf("")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (String, String) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E50A2))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("EXCUSA", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = rememberImagePainter("https://i.postimg.cc/NfcLn1tB/image-removebg-preview-65.png"),
                    contentDescription = "Logo UNICAH",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = usuario,
                    onValueChange = { if (it.all { c -> c.isDigit() }) usuario = it },
                    label = { Text("Usuario") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onLoginClick(usuario, contrasena) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E50A2))
                ) {
                    Text("Ingresar", color = Color.White)
                }
            }
        }
    }
}