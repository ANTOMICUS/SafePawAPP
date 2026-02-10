package com.example.safepaw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.safepaw.ui.theme.SafePawTheme
import com.safepaw.app.ui.navigation.SafePawNavigation
import com.safepaw.app.ui.viewmodels.AnimalViewModel
import com.safepaw.app.ui.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafePawTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val animalViewModel: AnimalViewModel = hiltViewModel()
                
                SafePawNavigation(
                    authViewModel = authViewModel,
                    animalViewModel = animalViewModel
                )
            }
        }
    }
}
