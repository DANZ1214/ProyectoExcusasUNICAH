package com.example.proyectoexusas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * MainActivity: La actividad principal de la aplicación.
 * Gestiona la configuración inicial, la navegación y la lógica de inicio de sesión.
 */
class MainActivity : ComponentActivity() {

    // Cliente HTTP Ktor para realizar solicitudes a la API.
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // CoroutineScope para manejar operaciones asíncronas.
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * onCreate: Se llama al crear la actividad.
     * Configura la interfaz de usuario y la navegación.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita el modo de borde a borde para una interfaz de usuario inmersiva.
        setContent {
            ProyectoExusasTheme {
                val navController = rememberNavController() // Controlador de navegación para la aplicación.
                Scaffold( // Proporciona una estructura básica de diseño con una barra superior y contenido.
                    topBar = { CustomTopBar() }, // Barra superior personalizada.
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost( // Contenedor para la navegación basada en destinos.
                        navController = navController,
                        startDestination = "login", // Destino inicial.
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") { // Definición de la pantalla de inicio de sesión.
                            Content(
                                onLoginClick = { usuario, contrasena ->
                                    login(usuario, contrasena, navController)
                                }
                            )
                        }
                        composable("excusa") { // Definición de la pantalla de excusas.
                            ExcusaScreen()
                        }
                    }
                }
            }
        }
    }

    /**
     * login: Realiza la solicitud de inicio de sesión a la API.
     *
     * @param usuario Nombre de usuario ingresado.
     * @param contrasena Contraseña ingresada.
     * @param navController Controlador de navegación para redirigir después del inicio de sesión.
     */
    private fun login(usuario: String, contrasena: String, navController: androidx.navigation.NavController) {
        scope.launch {
            try {
                val response = client.post("http://localhost:3008/api/unicah/user/login") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("userId" to usuario, "pass" to contrasena))
                }

                if (response.status.isSuccess()) {
                    navController.navigate("excusa") // Navega a la pantalla de excusas si el inicio de sesión es exitoso.
                } else {
                    val body = response.bodyAsText()
                    val json = Json.parseToJsonElement(body).jsonObject
                    val message = json["message"]?.jsonPrimitive?.content ?: "Usuario o contraseña incorrectos"
                    // Manejar el error, por ejemplo, mostrando un mensaje al usuario
                    println("Error de inicio de sesión: $message")
                    errorMsg = message;

                }
            } catch (e: Exception) {
                // Manejar errores de conexión
                println("Error de conexión: ${e.message}")
                errorMsg = "Error de conexión con el servidor";
            }
        }
    }
}

/**
 * CustomTopBar: Barra superior personalizada para la aplicación.
 */
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

// Variable para almacenar el mensaje de error del inicio de sesión.
var errorMsg : String = "";

/**
 * Content: Contenido de la pantalla de inicio de sesión.
 *
 * @param modifier Modificador para personalizar el diseño.
 * @param onLoginClick Función lambda para manejar el evento de clic en el botón de inicio de sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(
    modifier: Modifier = Modifier,
    onLoginClick: (String, String) -> Unit
) {
    var usuario by remember { mutableStateOf("") } // Estado para el nombre de usuario.
    var contrasena by remember { mutableStateOf("") } // Estado para la contraseña.

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
            modifier = Modifier.size(100.dp)
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