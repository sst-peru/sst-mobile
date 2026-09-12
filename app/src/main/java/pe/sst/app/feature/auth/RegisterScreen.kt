package pe.sst.app.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
import pe.sst.app.AppContainer
import pe.sst.app.ui.common.mensajeDeError

/**
 * Registro de trabajadores. Mismo endpoint que la web.
 *
 * Se pide el RUC porque es el dato que el trabajador conoce (boleta, cartel de obra) y evita
 * que alguien se registre en una empresa ajena eligiéndola de una lista. El rol no se pide:
 * quien se registra solo entra como operario.
 */
@Composable
fun RegisterScreen(
    container: AppContainer,
    onRegistered: () -> Unit,
    onCancel: () -> Unit,
) {
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var ruc by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var claveRepetida by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val completo = nombres.isNotBlank() && apellidos.isNotBlank() && ruc.length == 11 &&
        usuario.isNotBlank() && clave.length >= 8 && clave == claveRepetida

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Entrarás como operario. Si eres supervisor, pide que te creen la cuenta.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Campo("Nombres", nombres) { nombres = it }
        Campo("Apellidos", apellidos) { apellidos = it }
        Campo("DNI", dni, KeyboardType.Number) { if (it.length <= 8) dni = it }
        Campo("Teléfono", telefono, KeyboardType.Phone) { telefono = it }
        Campo("RUC de la empresa", ruc, KeyboardType.Number) { if (it.length <= 11) ruc = it }
        Campo("Usuario", usuario) { usuario = it }

        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        OutlinedTextField(
            value = claveRepetida,
            onValueChange = { claveRepetida = it },
            label = { Text("Repetir contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = claveRepetida.isNotEmpty() && claveRepetida != clave,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Button(
            onClick = {
                enviando = true
                error = null
                scope.launch {
                    try {
                        container.authRepository.register(
                            username = usuario.trim(),
                            password = clave,
                            passwordConfirm = claveRepetida,
                            firstName = nombres.trim(),
                            lastName = apellidos.trim(),
                            dni = dni,
                            phone = telefono,
                            companyRuc = ruc,
                        )
                        // Entramos directo: pedirle que escriba su clave otra vez sobra.
                        container.authRepository.login(usuario.trim(), clave)
                        onRegistered()
                    } catch (e: Exception) {
                        error = mensajeDeError(e)
                    }
                    enviando = false
                }
            },
            enabled = completo && !enviando,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            Text(if (enviando) "Creando cuenta…" else "Crear cuenta")
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text("Ya tengo cuenta")
        }
    }
}

@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    tipo: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = tipo),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    )
}
