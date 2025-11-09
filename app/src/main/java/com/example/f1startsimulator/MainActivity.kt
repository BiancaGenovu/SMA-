package com.example.f1startsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.f1startsimulator.ui.theme.F1StartSimulatorTheme
import kotlinx.coroutines.delay // Importăm funcția "delay"
import kotlinx.coroutines.launch // Importăm funcția "launch"
import kotlin.random.Random // Importăm generatorul de numere random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Am setat fundalul principal pe negru, ca să semene mai mult cu F1
        setContent {
            F1StartSimulatorTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black // Setăm fundalul negru
                ) { innerPadding ->
                    SemaforUI(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun SemaforUI(modifier: Modifier = Modifier) {

    // --- PASUL 1: Creăm "Starea" (State) ---
    // Aceasta este o listă "observabilă". Când schimbăm o culoare din ea,
    // Compose va redesena automat bulina corespunzătoare.
    // Inițial, toate sunt gri (stinse).
    val culoriBuline = remember {
        mutableStateListOf(
            Color.Gray,
            Color.Gray,
            Color.Gray,
            Color.Gray,
            Color.Gray
        )
    }

    // --- PASUL 2: Creăm un "Scope" pentru Corutine ---
    // Acesta este "motorul" care ne permite să rulăm logica cu întârzieri (delay).
    val coroutineScope = rememberCoroutineScope()

    // Funcția care pornește secvența de start
    fun pornesteStartul() {
        // Lansăm un "job" nou (o corutină) pe care îl putem rula în fundal
        coroutineScope.launch {
            // --- Logica Jocului (Săptămâna 2) ---

            // 1. Resetează toate bulinele la gri (pentru cazul în care jucăm din nou)
            for (i in 0..4) {
                culoriBuline[i] = Color.Gray
            }

            // 2. Aprinde bulinele secvențial
            for (i in 0..4) {
                delay(1000) // Așteaptă 1 secundă (1000 milisecunde)
                culoriBuline[i] = Color.Red // Aprinde bulina 'i'
            }

            // 3. Așteaptă un timp random înainte de a le stinge
            val timpRandom = Random.nextLong(1000, 4000) // Între 1 și 4 secunde
            delay(timpRandom)

            // 4. Stinge toate bulinele
            for (i in 0..4) {
                culoriBuline[i] = Color.Gray
            }

            // AICI VOM PORNI CRONOMETRUL MAI TÂRZIU
        }
    }


    // --- Interfața Grafică (UI) ---
    Column( // Am schimbat `Box`-ul exterior cu `Column` ca să putem pune butonul sub el
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Centrează totul pe ecran
    ) {

        // --- Acesta este suportul negru al semaforului ---
        Box(
            modifier = Modifier
                .background(Color.DarkGray, shape = RoundedCornerShape(12.dp)) // Rotunjim colțurile
                .padding(24.dp), // Spațiu în interiorul suportului
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val lightSpacing = 16.dp

                // --- PASUL 3: Modificăm bucla `for` ---
                // Acum bucla citește culoarea din lista "culoriBuline"
                for (i in 0..4) { // Buclele în programare încep de obicei de la 0
                    // Pasăm culoarea din "stare" către funcția noastră
                    BulinaLuminoasa(color = culoriBuline[i])

                    if (i < 4) { // Adaugă spațiu între 1-2, 2-3, 3-4, 4-5
                        Spacer(modifier = Modifier.height(lightSpacing))
                    }
                }
            }
        }

        // --- PASUL 4: Adăugăm Butonul "Start" ---
        Spacer(modifier = Modifier.height(32.dp)) // Spațiu între semafor și buton

        Button(
            onClick = {
                pornesteStartul() // Apelăm funcția noastră când se dă clic
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text(text = "START", fontSize = 20.sp)
        }
    }
}

// Funcția BulinaLuminoasa rămâne LA FEL
@Composable
fun BulinaLuminoasa(color: Color) {
    val lightSize = 60.dp

    Box(
        modifier = Modifier
            .size(lightSize)
            .clip(CircleShape)
            .background(color) // Folosim culoarea primită
    )
}

// Funcția Preview rămâne LA FEL
@Preview(showBackground = true, backgroundColor = 0xFF000000) // Am setat fundal negru la preview
@Composable
fun SemaforPreview() {
    F1StartSimulatorTheme {
        SemaforUI(modifier = Modifier.fillMaxSize())
    }
}