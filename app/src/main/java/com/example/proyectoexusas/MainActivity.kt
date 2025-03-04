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
                horizontalArrangement = Arrangement.Start // Cambiado a Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Reemplaza con tu logo
                    contentDescription = "Logo",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    )
}

@Composable
fun Content(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "Imagen Casilla 1") // Reemplaza
            TextField(value = "", onValueChange = {}, placeholder = { Text("Usuario") })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(value = "", onValueChange = {}, placeholder = { Text("Contraseña") })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Lógica del botón */ }) {
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
        ){ innerPadding ->
            Content(modifier = Modifier.padding(innerPadding))
        }
    }
}