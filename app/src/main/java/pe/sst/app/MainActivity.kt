package pe.sst.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pe.sst.app.ui.navigation.SstApp
import pe.sst.app.ui.theme.SstTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SstTheme {
                SstApp(container = (application as SstApplication).container)
            }
        }
    }
}
