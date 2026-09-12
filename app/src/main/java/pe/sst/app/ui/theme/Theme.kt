package pe.sst.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Naranja de seguridad industrial: es el color que el operario asocia con SST.
private val Safety = Color(0xFFE65100)
private val SafetyDark = Color(0xFFFF8A3D)
private val Critical = Color(0xFFC62828)

private val LightColors = lightColorScheme(
    primary = Safety,
    secondary = Color(0xFF37474F),
    error = Critical,
)

private val DarkColors = darkColorScheme(
    primary = SafetyDark,
    secondary = Color(0xFFB0BEC5),
    error = Color(0xFFEF9A9A),
)

@Composable
fun SstTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
