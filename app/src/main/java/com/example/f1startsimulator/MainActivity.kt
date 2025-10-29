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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.f1startsimulator.ui.theme.F1StartSimulatorTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            F1StartSimulatorTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

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
fun BulinaSemafor(color: Color) {
    val marimeBulina = 60.dp

    Box(
        modifier = Modifier
            .size(marimeBulina)
            .clip(CircleShape)
            .background(color)
    )

}

@Composable
fun SemaforUI(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.DarkGray)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val distantaBuline = 16.dp

            for (i in 1..5) {
                BulinaSemafor(color = Color.Gray)

                if (i < 5) {
                    Spacer(modifier = Modifier.height(distantaBuline))
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SemaforPreview() {
    F1StartSimulatorTheme {
        SemaforUI(modifier = Modifier.fillMaxSize())
    }
}