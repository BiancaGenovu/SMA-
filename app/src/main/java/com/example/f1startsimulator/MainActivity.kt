package com.example.f1startsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.f1startsimulator.ui.theme.F1StartSimulatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- PASUL 1: Definim stările posibile ale jocului ---
enum class StareJoc {
    GATA,       // Așteaptă apăsarea butonului "Start"
    START,      // Luminile se aprind secvențial
    CURSA,      // Luminile s-au stins, cronometrul merge
    FURAT,      // Start furat (ai apăsat prea devreme)
    TERMINAT    // Timpul a fost înregistrat
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            F1StartSimulatorTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black
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

    val culoriBuline = remember {
        mutableStateListOf(Color.Gray, Color.Gray, Color.Gray, Color.Gray, Color.Gray)
    }
    val coroutineScope = rememberCoroutineScope()

    // --- PASUL 2: Variabile noi pentru Stare și Timp ---
    var stareJoc by remember { mutableStateOf(StareJoc.GATA) }
    var timpReactie by remember { mutableStateOf(0L) } // 0L înseamnă 0 Long (numeric)
    var timpStart by remember { mutableStateOf(0L) }

    fun pornesteStartul() {
        coroutineScope.launch {
            // 1. Resetează tot
            timpReactie = 0L
            culoriBuline.fill(Color.Gray)
            stareJoc = StareJoc.START // Trecem în starea "START"

            // 2. Aprinde luminile secvențial
            for (i in 0..4) {
                // Dacă jucătorul a furat startul, oprim corutina
                if (stareJoc == StareJoc.FURAT) return@launch
                delay(1000)
                culoriBuline[i] = Color.Red
            }

            // 3. Așteaptă un timp random
            val timpRandom = Random.nextLong(1000, 4000)
            delay(timpRandom)

            // Verificăm din nou dacă a furat startul
            if (stareJoc == StareJoc.FURAT) return@launch

            // 4. Stinge luminile și pornește cronometrul
            culoriBuline.fill(Color.Gray)
            stareJoc = StareJoc.CURSA // Acum începe cursa!
            timpStart = System.currentTimeMillis() // Salvăm timpul EXACT
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            // Adăugăm .clickable la Column
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Fără efect vizual la clic
            ) {

                // Când se apasă oriunde pe ecran:

                // 1. Dacă apăsăm în timp ce cronometrul merge (corect)
                if (stareJoc == StareJoc.CURSA) {
                    timpReactie = System.currentTimeMillis() - timpStart // Calculăm diferența
                    stareJoc = StareJoc.TERMINAT
                }
                // 2. Dacă apăsăm prea devreme (în timp ce luminile se aprindeau)
                else if (stareJoc == StareJoc.START) {
                    stareJoc = StareJoc.FURAT
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {


        Box(
            modifier = Modifier
                .background(Color.DarkGray, shape = RoundedCornerShape(12.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val lightSpacing = 16.dp

                for (i in 0..4) {
                    BulinaLuminoasa(color = culoriBuline[i])
                    if (i < 4) {
                        Spacer(modifier = Modifier.height(lightSpacing))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Butonul "Start"
        Button(
            enabled = (stareJoc == StareJoc.GATA || stareJoc == StareJoc.TERMINAT || stareJoc == StareJoc.FURAT),
            onClick = {
                pornesteStartul()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text(text = "START", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))


        if (stareJoc == StareJoc.TERMINAT) {
            Text(
                text = "${timpReactie} ms", // Afișăm timpul în milisecunde
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (stareJoc == StareJoc.FURAT) {
            Text(
                text = "START FURAT!",
                color = Color.Red,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun BulinaLuminoasa(color: Color) {
    val lightSize = 60.dp

    Box(
        modifier = Modifier
            .size(lightSize)
            .clip(CircleShape)
            .background(color)
    )
}


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SemaforPreview() {
    F1StartSimulatorTheme {
        SemaforUI(modifier = Modifier.fillMaxSize())
    }
}