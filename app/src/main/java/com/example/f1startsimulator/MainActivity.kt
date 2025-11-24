package com.example.f1startsimulator

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.f1startsimulator.ui.theme.F1StartSimulatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class StareJoc {
    GATA, START, CURSA, MOTOR_PORNIT, FURAT, ORDINE_GRESITA, TERMINAT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            F1StartSimulatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color.Black) { innerPadding ->
                    SemaforUI(modifier = Modifier.padding(innerPadding).fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun SemaforUI(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // --- CODAREA SUNETELOR ---
    // Folosim 'remember' ca să nu recreăm player-ul la fiecare milisecundă
    // dar îl facem null-abil ca să îl putem elibera.
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Funcție helper ca să redăm ușor un sunet
    fun redaSunet(idResursa: Int, loop: Boolean = false) {
        // Oprim sunetul anterior dacă există
        mediaPlayer?.release()

        // Creăm și pornim noul sunet
        mediaPlayer = MediaPlayer.create(context, idResursa)
        mediaPlayer?.isLooping = loop // Dacă vrem să se repete (pt motor la relanti)
        mediaPlayer?.start()
    }

    // Când părăsim ecranul, oprim orice sunet (curățenie)
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
    // -------------------------

    val culoriBuline = remember { mutableStateListOf(Color.Gray, Color.Gray, Color.Gray, Color.Gray, Color.Gray) }
    val coroutineScope = rememberCoroutineScope()

    var stareJoc by remember { mutableStateOf(StareJoc.GATA) }
    var timpReactie by remember { mutableLongStateOf(0L) }
    var timpStart by remember { mutableLongStateOf(0L) }

    DisposableEffect(stareJoc) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val fortaTotala = kotlin.math.sqrt(x * x + y * y + z * z)
                val aMiscatBrusc = fortaTotala > 14.0

                if (aMiscatBrusc) {
                    if (stareJoc == StareJoc.CURSA) {
                        stareJoc = StareJoc.ORDINE_GRESITA
                        // SUNET: Eroare (ai accelerat prea devreme)
                        redaSunet(R.raw.error)
                    }
                    else if (stareJoc == StareJoc.MOTOR_PORNIT) {
                        timpReactie = System.currentTimeMillis() - timpStart
                        stareJoc = StareJoc.TERMINAT
                        // SUNET: Vroom! (Succes)
                        redaSunet(R.raw.engine_start)
                    }
                }
            }
        }

        if (stareJoc == StareJoc.CURSA || stareJoc == StareJoc.MOTOR_PORNIT) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    fun pornesteStartul() {
        // Oprim orice sunet vechi când începe jocul nou
        mediaPlayer?.release()
        mediaPlayer = null

        coroutineScope.launch {
            timpReactie = 0L
            culoriBuline.fill(Color.Gray)
            stareJoc = StareJoc.START

            for (i in 0..4) {
                if (stareJoc == StareJoc.FURAT) return@launch
                delay(1000)
                culoriBuline[i] = Color.Red
            }

            val timpRandom = Random.nextLong(1000, 4000)
            delay(timpRandom)

            if (stareJoc == StareJoc.FURAT) return@launch

            culoriBuline.fill(Color.Gray)
            stareJoc = StareJoc.CURSA
            timpStart = System.currentTimeMillis()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // LOGICA DE TAP
                if (stareJoc == StareJoc.CURSA) {
                    stareJoc = StareJoc.MOTOR_PORNIT
                    // SUNET: Motor la relanti (looping = true)
                    // Va merge continuu până când accelerezi sau greșești
                    redaSunet(R.raw.engine_idle, loop = true)
                }
                else if (stareJoc == StareJoc.START) {
                    stareJoc = StareJoc.FURAT
                    // SUNET: Eroare (Start furat)
                    redaSunet(R.raw.error)
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

        Button(
            enabled = (stareJoc != StareJoc.START && stareJoc != StareJoc.CURSA && stareJoc != StareJoc.MOTOR_PORNIT),
            onClick = { pornesteStartul() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text(text = "START", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (stareJoc) {
            StareJoc.TERMINAT -> {
                Text(text = "${timpReactie} ms", color = Color.Green, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Text(text = "Vroom!", color = Color.White, fontSize = 20.sp)
            }
            StareJoc.FURAT -> {
                Text(text = "START FURAT!", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            StareJoc.ORDINE_GRESITA -> {
                Text(text = "NU AI PORNIT MOTORUL!", color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            StareJoc.MOTOR_PORNIT -> {
                Text(text = "Motor Pornit! Accelerează!", color = Color.Yellow, fontSize = 24.sp)
            }
            else -> {}
        }
    }
}

@Composable
fun BulinaLuminoasa(color: Color) {
    val lightSize = 60.dp
    Box(modifier = Modifier.size(lightSize).clip(CircleShape).background(color))
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SemaforPreview() {
    F1StartSimulatorTheme {
        SemaforUI(modifier = Modifier.fillMaxSize())
    }
}