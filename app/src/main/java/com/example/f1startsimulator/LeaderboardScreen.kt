package com.example.f1startsimulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Structura simplă a unui scor
data class JucatorScor(
    val email: String,
    val timp: Long
)

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    // Lista care va ține scorurile descărcate
    val listaScoruri = remember { mutableStateListOf<JucatorScor>() }
    var isLoading by remember { mutableStateOf(true) }

    // Descărcăm datele imediat ce se deschide ecranul
    LaunchedEffect(Unit) {
        db.collection("clasament")
            .orderBy("timp", Query.Direction.ASCENDING) // Cel mai mic timp primul
            .limit(20) // Luăm doar top 20
            .get()
            .addOnSuccessListener { result ->
                listaScoruri.clear()
                for (document in result) {
                    val email = document.getString("email") ?: "Anonim"
                    val timp = document.getLong("timp") ?: 0L
                    listaScoruri.add(JucatorScor(email, timp))
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Titlu
        Text(
            text = "🏆 Top Piloți 🏆",
            color = Color.Yellow,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // Lista scrollabilă
            LazyColumn {
                items(listaScoruri) { scor ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = scor.email, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = "${scor.timp} ms", color = Color.Green, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Înapoi la Curse", color = Color.Black)
        }
    }
}