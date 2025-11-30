package com.example.f1startsimulator

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.f1startsimulator.ui.theme.F1StartSimulatorTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("game") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("game") {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                                SemaforUI(
                                    onNavigateToLeaderboard = {
                                        navController.navigate("leaderboard")
                                    }
                                )
                            }
                        }
                        composable("leaderboard") {
                            LeaderboardScreen(
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SemaforUI(
    modifier: Modifier = Modifier,
    onNavigateToLeaderboard: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun redaSunet(idResursa: Int, loop: Boolean = false) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, idResursa)
        mediaPlayer?.setVolume(1.0f, 1.0f)
        mediaPlayer?.isLooping = loop
        mediaPlayer?.start()
    }

    fun salveazaScorInCloud(timpNou: Long) {
        if (user != null && user.email != null) {
            // Folosim emailul ca ID unic al documentului, ca să nu avem duplicate
            val referintaScor = db.collection("clasament").document(user.email!!)

            referintaScor.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Cazul 1: Utilizatorul a mai jucat. Verificăm timpul vechi.
                    val timpVechi = document.getLong("timp") ?: Long.MAX_VALUE

                    if (timpNou < timpVechi) {
                        // URRA! E un timp mai bun (mai mic). Actualizăm!
                        referintaScor.update("timp", timpNou, "data", java.util.Date())
                        Toast.makeText(context, "Record Personal Nou: $timpNou ms!", Toast.LENGTH_LONG).show()
                    } else {
                        // Cazul 2: Timpul e mai slab. Nu schimbăm nimic în baza de date.
                        Toast.makeText(context, "Nu ai bătut recordul ($timpVechi ms)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Cazul 3: E prima dată când joacă. Salvăm direct.
                    val scorData = hashMapOf(
                        "email" to user.email,
                        "timp" to timpNou,
                        "data" to java.util.Date()
                    )
                    // Folosim .set() în loc de .add() pentru a specifica noi ID-ul (emailul)
                    referintaScor.set(scorData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Primul scor salvat!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    DisposableEffect(Unit) { onDispose { mediaPlayer?.release() } }

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
                val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
                if (kotlin.math.sqrt(x * x + y * y + z * z) > 14.0) {
                    if (stareJoc == StareJoc.CURSA) {
                        stareJoc = StareJoc.ORDINE_GRESITA; redaSunet(R.raw.error)
                    } else if (stareJoc == StareJoc.MOTOR_PORNIT) {
                        val timpFinal = System.currentTimeMillis() - timpStart
                        timpReactie = timpFinal
                        stareJoc = StareJoc.TERMINAT
                        redaSunet(R.raw.engine_start)
                        salveazaScorInCloud(timpFinal)
                    }
                }
            }
        }
        if (stareJoc == StareJoc.CURSA || stareJoc == StareJoc.MOTOR_PORNIT) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    fun pornesteStartul() {
        mediaPlayer?.release(); mediaPlayer = null
        coroutineScope.launch {
            timpReactie = 0L; culoriBuline.fill(Color.Gray); stareJoc = StareJoc.START
            for (i in 0..4) {
                if (stareJoc == StareJoc.FURAT) return@launch
                delay(1000); culoriBuline[i] = Color.Red
            }
            delay(Random.nextLong(1000, 4000))
            if (stareJoc == StareJoc.FURAT) return@launch
            culoriBuline.fill(Color.Gray); stareJoc = StareJoc.CURSA; timpStart = System.currentTimeMillis()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) {
            if (stareJoc == StareJoc.CURSA) {
                stareJoc = StareJoc.MOTOR_PORNIT; redaSunet(R.raw.engine_idle, loop = true)
            } else if (stareJoc == StareJoc.START) {
                stareJoc = StareJoc.FURAT; redaSunet(R.raw.error)
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.background(Color.DarkGray, RoundedCornerShape(12.dp)).padding(24.dp)) {
            Column {
                for (i in 0..4) {
                    BulinaLuminoasa(color = culoriBuline[i])
                    if (i < 4) Spacer(Modifier.height(16.dp))
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(
            enabled = (stareJoc != StareJoc.START && stareJoc != StareJoc.CURSA && stareJoc != StareJoc.MOTOR_PORNIT),
            onClick = { pornesteStartul() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) { Text("START", fontSize = 20.sp) }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onNavigateToLeaderboard,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black)
        ) { Text("🏆 Vezi Clasament 🏆") }

        Spacer(Modifier.height(32.dp))

        // AICI ERA EROAREA - ACUM E CORECTAT (am adăugat "color =")
        when (stareJoc) {
            StareJoc.TERMINAT -> {
                Text(text = "${timpReactie} ms", color = Color.Green, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Text(text = "Scor Salvat!", color = Color.White, fontSize = 20.sp)
            }
            StareJoc.FURAT -> {
                Text(text = "START FURAT!", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            StareJoc.ORDINE_GRESITA -> {
                Text(text = "NU AI PORNIT MOTORUL!", color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            StareJoc.MOTOR_PORNIT -> {
                Text(text = "Motor Pornit!", color = Color.Yellow, fontSize = 24.sp)
            }
            else -> {}
        }
    }
}

@Composable
fun BulinaLuminoasa(color: Color) {
    Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(color))
}