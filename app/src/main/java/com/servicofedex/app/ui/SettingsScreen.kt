package com.servicofedex.app.ui

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import com.servicofedex.app.AppUiState
import com.servicofedex.app.MainViewModel
import com.servicofedex.app.data.AppSettings
import com.servicofedex.app.ui.components.AppCard
import com.servicofedex.app.ui.components.SectionTitle
import com.servicofedex.app.ui.components.StatLine
import com.servicofedex.app.util.StatsCalculator
import java.io.File

@Composable
fun SettingsScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val settings = state.settings

    var serviceText by remember { mutableStateOf(StatsCalculator.formatNumber(settings.serviceValue, 2)) }
    var supplyText by remember { mutableStateOf(StatsCalculator.formatNumber(settings.supplyValue, 2)) }
    var vehicle by remember { mutableStateOf(settings.vehicleName) }
    var autonomyText by remember { mutableStateOf(StatsCalculator.formatNumber(settings.autonomy, 2)) }
    var userName by remember { mutableStateOf(settings.userName) }
    var darkTheme by remember { mutableStateOf(settings.darkTheme) }
    var error by remember { mutableStateOf<String?>(null) }
    var showConfirm by remember { mutableStateOf(false) }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val dir = File(context.filesDir, "profile").apply { mkdirs() }
                    val file = File(dir, "user.jpg")
                    file.writeBytes(bytes)
                    viewModel.updateSettings { it.copy(photoPath = file.absolutePath) }
                    Toast.makeText(context, "Foto atualizada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Não foi possível ler a imagem: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val photoBitmap = remember(settings.photoPath) {
        settings.photoPath?.let { path ->
            val file = File(path)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        AppCard {
            SectionTitle("Valores pagos")
            OutlinedTextField(
                value = serviceText,
                onValueChange = { serviceText = it; error = null },
                label = { Text("Valor da ordem de serviço (R$)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = supplyText,
                onValueChange = { supplyText = it; error = null },
                label = { Text("Valor do suprimento (R$)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            StatLine("Padrão de fábrica", "serviço R$ 16,00 | suprimento R$ 5,00")
        }

        AppCard {
            SectionTitle("Veículo")
            OutlinedTextField(
                value = vehicle,
                onValueChange = { vehicle = it; error = null },
                label = { Text("Nome do veículo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = autonomyText,
                onValueChange = { autonomyText = it; error = null },
                label = { Text("Autonomia (km por litro)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            StatLine("Uso da autonomia", "Estimativa de consumo em home, histórico e PDF")
        }

        AppCard {
            SectionTitle("Perfil")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (photoBitmap != null) {
                    Image(
                        bitmap = photoBitmap,
                        contentDescription = "Foto do usuário",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Sem foto",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp),
                    )
                }
                OutlinedButton(onClick = { pickPhoto.launch("image/*") }) {
                    Text("Escolher foto")
                }
            }
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it; error = null },
                label = { Text("Nome do usuário") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AppCard {
            SectionTitle("Aparência")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (darkTheme) "Tema escuro" else "Tema claro",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = darkTheme,
                    onCheckedChange = { checked ->
                        darkTheme = checked
                        viewModel.updateSettings { it.copy(darkTheme = checked) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            }
        }

        if (error != null) {
            Text(text = error.orEmpty(), style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = {
                val service = serviceText.replace(",", ".").toDoubleOrNull()
                val supply = supplyText.replace(",", ".").toDoubleOrNull()
                val autonomy = autonomyText.replace(",", ".").toDoubleOrNull()
                when {
                    service == null || service < 0.0 -> error = "Valor da ordem inválido."
                    supply == null || supply < 0.0 -> error = "Valor do suprimento inválido."
                    autonomy == null || autonomy < 0.0 -> error = "Autonomia inválida."
                    else -> {
                        viewModel.updateSettings { current: AppSettings ->
                            current.copy(
                                serviceValue = service,
                                supplyValue = supply,
                                vehicleName = vehicle.ifBlank { "Veículo não informado" },
                                autonomy = autonomy,
                                userName = userName.ifBlank { "Usuário" },
                            )
                        }
                        error = null
                        Toast.makeText(context, "Configurações salvas", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("Salvar configurações")
        }

        OutlinedButton(
            onClick = { showConfirm = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Limpar todo o histórico")
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Excluir histórico?") },
            text = { Text("Todos os registros de dias serão removidos. Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearRecords()
                    showConfirm = false
                    Toast.makeText(context, "Histórico excluído", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Excluir")
                }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancelar") } },
        )
    }
}
