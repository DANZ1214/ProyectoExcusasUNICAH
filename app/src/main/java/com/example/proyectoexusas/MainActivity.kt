package com.example.proyectoexusas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectoexusas.ui.theme.ProyectoExusasTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoExusasTheme {
                Scaffold(
                    topBar = { CustomTopBar() },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Content(modifier = Modifier.padding(innerPadding))
                }
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
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), //cambiar para agregar el logo de la U
                    contentDescription = "Logo",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(modifier: Modifier = Modifier) {
    var usuario by remember { mutableStateOf("") } //variable global para recordar el usuario
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "Logo Unicah")
            OutlinedTextField(
                value = usuario,
                onValueChange = { newValue ->
                    if (newValue.all { char -> char.isDigit() }) {
                        usuario = newValue
                    }
                },
                label = { Text("Usuario") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Hace que solo se pueda usar el teclado numerico
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray
                ),
                textStyle = TextStyle(fontSize = 18.sp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray
                ),
                textStyle = TextStyle(fontSize = 18.sp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            println("Usuario ingresado: $usuario") //cambiar para redirigir a la siguiente pantalla de alumno o maestro
        }) {
            Text("Ingresar")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ProyectoExusasTheme {
        Scaffold(
            topBar = { CustomTopBar() }
        ) { innerPadding ->
            Content(modifier = Modifier.padding(innerPadding))
        }
    }
}