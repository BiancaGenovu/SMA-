package com.example.f1startsimulator

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance() // "Cheia" către Firebase

    // Variabile de stare pentru text
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Variabilă pentru erori (ex: "Parolă greșită")
    var errorMessage by remember { mutableStateOf("") }

    // Variabilă pentru încărcare (să arătăm o rotiță cât timp vorbește cu serverul)
    var isLoading by remember { mutableStateOf(false) }

    // Variabilă care decide dacă suntem pe ecranul de Login sau Register
    // true = Login, false = Register
    var isLoginMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titlul se schimbă în funcție de mod
        Text(
            text = if (isLoginMode) "Autentificare F1" else "Creează Cont",
            fontSize = 32.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Câmp Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Câmp Parolă
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Parolă") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Afișăm eroarea cu roșu, dacă există
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dacă se încarcă, arătăm rotița, altfel arătăm butonul
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    // 1. Validare simplă
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Completează toate câmpurile!"
                        return@Button
                    }

                    isLoading = true // Pornim rotița
                    errorMessage = "" // Ștergem erorile vechi

                    if (isLoginMode) {
                        // --- LOGICA DE LOGIN ---
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false // Oprim rotița
                                if (task.isSuccessful) {
                                    // Succes! Navigăm la joc
                                    onLoginSuccess()
                                } else {
                                    // Eroare (ex: parolă greșită)
                                    errorMessage = "Eroare: ${task.exception?.message}"
                                }
                            }
                    } else {
                        // --- LOGICA DE ÎNREGISTRARE ---
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    // Cont creat cu succes! Navigăm direct.
                                    Toast.makeText(context, "Cont creat!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Eroare: ${task.exception?.message}"
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                // Textul de pe buton se schimbă
                Text(if (isLoginMode) "Intră în cursă" else "Înregistrează-te")
            }
        }

        // Butonul de jos pentru a schimba modul (Login <-> Register)
        TextButton(onClick = {
            isLoginMode = !isLoginMode // Schimbăm starea (true devine false și invers)
            errorMessage = "" // Curățăm erorile când schimbăm ecranul
        }) {
            Text(if (isLoginMode) "Nu ai cont? Înregistrează-te" else "Ai deja cont? Autentifică-te")
        }
    }
}