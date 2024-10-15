package com.timmitof.keyboardfonts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.timmitof.common.ui.theme.KeyboardFontsTheme
import com.timmitof.keyboardfonts.ui.MainScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Главная активность приложения
 */
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            KeyboardFontsTheme {
                val navHostController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(activity = this)

                MainScreen(
                    navHostController = navHostController,
                    windowWidthSizeClass = windowSizeClass.widthSizeClass
                )
            }
        }
    }
}