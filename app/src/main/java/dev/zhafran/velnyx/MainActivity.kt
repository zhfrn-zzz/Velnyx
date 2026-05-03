package dev.zhafran.velnyx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.navigation.VelnyxNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VelnyxTheme {
                val navController = rememberNavController()
                VelnyxNavHost(navController)
            }
        }
    }
}
