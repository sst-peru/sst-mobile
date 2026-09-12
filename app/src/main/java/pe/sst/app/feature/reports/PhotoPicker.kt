package pe.sst.app.feature.reports

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import java.io.File

/**
 * Botón de foto + captura de GPS.
 *
 * Pedimos la ubicación junto con la foto, no antes: así el permiso aparece en el momento en
 * que el operario entiende para qué sirve, y si lo rechaza el reporte se envía igual sin GPS.
 */
@Composable
fun PhotoPicker(
    photoPath: String?,
    onPhoto: (String?) -> Unit,
    onLocation: (Double?, Double?) -> Unit,
) {
    val context = LocalContext.current
    val photoFile = remember { createPhotoFile(context) }
    val photoUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
    }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) requestLocation(context, onLocation)
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onPhoto(photoFile.absolutePath)
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column {
        Button(
            onClick = { takePicture.launch(photoUri) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(if (photoPath == null) "Tomar foto" else "Tomar otra foto")
        }

        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = "Evidencia del hallazgo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(top = 12.dp),
            )
        } else {
            Text(
                "La foto es lo que permite al comité entender el peligro sin ir al lugar.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private fun createPhotoFile(context: Context): File {
    val directory = File(context.cacheDir, "fotos").apply { mkdirs() }
    return File(directory, "reporte_${System.currentTimeMillis()}.jpg")
}

private fun requestLocation(context: Context, onLocation: (Double?, Double?) -> Unit) {
    runCatching {
        LocationServices.getFusedLocationProviderClient(context).lastLocation
            .addOnSuccessListener { location ->
                onLocation(location?.latitude, location?.longitude)
            }
    }
}
