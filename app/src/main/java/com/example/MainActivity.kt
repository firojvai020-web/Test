package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.WarriorsGameScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WarriorsViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: WarriorsViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          androidx.compose.foundation.layout.Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            WarriorsGameScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}

