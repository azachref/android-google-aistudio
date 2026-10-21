package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MixerScreen
import com.example.ui.theme.SomniaTheme
import com.example.ui.viewmodel.SoundMixerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SoundMixerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SomniaTheme {
                MixerScreen(viewModel = viewModel)
            }
        }
    }
}
