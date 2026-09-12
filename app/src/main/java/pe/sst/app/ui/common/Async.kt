package pe.sst.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Estado de una carga contra el API. */
sealed interface Async<out T> {
    data object Loading : Async<Nothing>
    data class Ok<T>(val data: T) : Async<T>
    data class Failed(val message: String) : Async<Nothing>
}

/**
 * Carga datos del API en una pantalla sin tener que escribir un ViewModel por cada una.
 *
 * Devuelve el estado y una función para recargar: se usa después de asignar, cerrar o
 * completar algo, para que la lista refleje el cambio.
 */
@Composable
fun <T> rememberAsync(
    key: Any? = Unit,
    block: suspend () -> T,
): Pair<MutableState<Async<T>>, () -> Unit> {
    val estado = remember(key) { mutableStateOf<Async<T>>(Async.Loading) }
    val recarga = remember(key) { mutableStateOf(0) }

    LaunchedEffect(key, recarga.value) {
        estado.value = Async.Loading
        estado.value = try {
            Async.Ok(block())
        } catch (error: Exception) {
            Async.Failed(mensajeDeError(error))
        }
    }
    val recargar: () -> Unit = { recarga.value += 1 }
    return Pair(estado, recargar)
}

/** Traduce la excepción a algo que el operario pueda entender. */
fun mensajeDeError(error: Exception): String {
    val texto = error.message ?: "Error desconocido"
    return when {
        "403" in texto -> "No tienes permiso para ver esto."
        "401" in texto -> "Tu sesión expiró. Vuelve a ingresar."
        "Unable to resolve host" in texto || "timeout" in texto.lowercase() ->
            "Sin conexión con el servidor. Revisa la red."
        else -> texto
    }
}

/** Envoltura estándar: rueda mientras carga, mensaje con reintento si falla. */
@Composable
fun <T> AsyncContent(
    state: Async<T>,
    onRetry: () -> Unit,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        is Async.Loading -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { CircularProgressIndicator() }

        is Async.Failed -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                state.message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                Text("Reintentar")
            }
        }

        is Async.Ok -> content(state.data)
    }
}

/** Etiqueta corta de estado o nivel, con el color que le corresponde. */
@Composable
fun Badge(texto: String, critico: Boolean = false, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = if (critico) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}
