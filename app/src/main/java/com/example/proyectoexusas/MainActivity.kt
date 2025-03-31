package com.example.proyectoexusas

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.proyectoexusas.ui.theme.ProyectoExusasTheme
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            selectedFileUri = uri
        }

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
                            Content(
                                onLoginClick = { usuario, contrasena ->
                                    login(usuario, contrasena, navController)
                                }
                            )
                        }
                        composable("excusa/{alumnoId}") { backStackEntry ->
                            val alumnoId = backStackEntry.arguments?.getString("alumnoId")?.toIntOrNull() ?: 0
                            ExcusaScreen(
                                onSelectFile = { getContent.launch("*/*") },
                                fileName = selectedFileUri?.lastPathSegment ?: "Seleccionar archivo",
                                alumnoId = alumnoId
                            )
                        }
                    }
                }
            }
        }
    }

    fun login(usuario: String, contrasena: String, navController: androidx.navigation.NavController) {
        scope.launch {
            try {
                val apiUrl = "http://172.28.0.1:3008/api/unicah/user/login"

                val response = client.post(apiUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("userId" to usuario, "pass" to contrasena))
                }

                val body = response.bodyAsText()
                val json = Json.parseToJsonElement(body).jsonObject

                if (response.status.isSuccess()) {
                    val alumnoId = json["alumnoId"]?.jsonPrimitive?.intOrNull

                    if (alumnoId != null && alumnoId > 0) {
                        navController.navigate("excusa/$alumnoId")
                    } else {
                        errorMsg = "Este usuario no está registrado como alumno"
                    }
                } else {
                    val message = json["message"]?.jsonPrimitive?.content ?: "Usuario o contraseña incorrectos"
                    println("Error de inicio de sesión: $message")
                    errorMsg = message
                }
            } catch (e: Exception) {
                println("Error de conexión: ${e.message}")
                errorMsg = "Error de conexión con el servidor"
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

var errorMsg: String = ""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(
    modifier: Modifier = Modifier,
    onLoginClick: (String, String) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberImagePainter("https://i.postimg.cc/NfcLn1tB/image-removebg-preview-65.png"),
            contentDescription = "Logo Unicah",
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { newValue ->
                if (newValue.all { char -> char.isDigit() }) {
                    usuario = newValue
                }
            },
            label = { Text("Usuario", color = Color.Black) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF1E50A2),
                unfocusedBorderColor = Color(0xFF1E50A2)
            ),
            textStyle = TextStyle(fontSize = 18.sp, color = Color.Black)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña", color = Color.Black) },
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF1E50A2),
                unfocusedBorderColor = Color(0xFF1E50A2)
            ),
            textStyle = TextStyle(fontSize = 18.sp, color = Color.Black)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = errorMsg, color = Color.Red)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onLoginClick(usuario, contrasena)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E50A2))
        ) {
            Text("Ingresar", color = Color.White)
        }
    }
}
