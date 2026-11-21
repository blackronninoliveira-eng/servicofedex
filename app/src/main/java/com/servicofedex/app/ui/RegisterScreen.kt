package com.servicofedex.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.servicofedex.app.AppUiState
import com.servicofedex.app.MainViewModel
import com.servicofedex.app.data.DailyRecord
import com.servicofedex.app.ui.components.AppCard
import com.servicofedex.app.ui.components.SectionTitle
import com.servicofedex.app.ui.components.StatLine
import com.servicofedex.app.util.DateUtils
import com.servicofedex.app.util.StatsCalculator

@Composable
fun RegisterScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val today = DateUtils.iso(DateUtils.today())
    val existing = state.records.find { it.date == today }

    var date by remember { mutableStateOf(today) }
    var done by remember { mutableStateOf(existing?.servicesDone?.toString() ?: "") }
    var canceled by remember { mutableStateOf(existing?.servicesCanceled?.toString() ?: "") }
    var supplies by remember { mutableStateOf(existing?.suppliesDone?.toString() ?: "") }
    var kmStart by remember { mutableStateOf(existing?.kmStart?.let { formatInput(it) } ?: "") }
    var kmEnd by remember { mutableStateOf(existing?.kmEnd?.let { formatInput(it) } ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    val doneValue = done.toIntOrNull() ?: 0
    val canceledValue = canceled.toIntOrNull() ?: 0
    val suppliesValue = supplies.toIntOrNull() ?: 0
    val startValue = kmStart.replace(",", ".").toDoubleOrNull() ?: 0.0
    val endValue = kmEnd.replace(",", ".").toDoubleOrNull() ?: 0.0
    val previewRecord = DailyRecord(
        date = date,
        servicesDone = doneValue,
        servicesCanceled = canceledValue,
        suppliesDone = suppliesValue,
        kmStart = startValue,
        kmEnd = endValue,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Registro do dia",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Informe as quantidades e a quilometragem. O valor é calculado automaticamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        AppCard {
            SectionTitle("Data")
            OutlinedTextField(
                value = date,
                onValueChange = { date = it; error = null },
                label = { Text("Data (AAAA-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            SectionTitle("Ordens de serviço")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    value = done,
                    onValueChange = { done = it; error = null },
                    label = "Feitas",
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    value = canceled,
                    onValueChange = { canceled = it; error = null },
                    label = "Canceladas",
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    value = supplies,
                    onValueChange = { supplies = it; error = null },
                    label = "Suprimentos",
                    modifier = Modifier.weight(1f),
                )
            }

            SectionTitle("Quilometragem")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    value = kmStart,
                    onValueChange = { kmStart = it; error = null },
                    label = "KM inicial",
                    decimal = true,
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    value = kmEnd,
                    onValueChange = { kmEnd = it; error = null },
                    label = "KM final",
                    decimal = true,
                    modifier = Modifier.weight(1f),
                )
            }
            StatLine(
                "Distância percorrida",
                "${StatsCalculator.formatNumber(previewRecord.kmRun)} km",
            )
        }

        AppCard(containerColor = MaterialTheme.colorScheme.primary) {
            Text(
                text = "TOTAL DESSA ENTRADA",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = "${previewRecord.totalOrders()} ordens",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = StatsCalculator.formatCurrency(previewRecord.amount(state.settings)),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = "(${doneValue} x ${StatsCalculator.formatCurrency(state.settings.serviceValue)}) + " +
                    "(${suppliesValue} x ${StatsCalculator.formatCurrency(state.settings.supplyValue)})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        if (error != null) {
            Text(
                text = error.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(
            onClick = {
                val parsedDate = DateUtils.parse(date)
                when {
                    parsedDate == null -> error = "Informe uma data válida no formato AAAA-MM-DD."
                    done.toIntOrNull() == null && done.isNotBlank() -> error = "Quantidade de serviços inválida."
                    canceled.toIntOrNull() == null && canceled.isNotBlank() -> error = "Quantidade de cancelamentos inválida."
                    supplies.toIntOrNull() == null && supplies.isNotBlank() -> error = "Quantidade de suprimentos inválida."
                    kmStart.replace(",", ".").toDoubleOrNull() == null && kmStart.isNotBlank() -> error = "KM inicial inválido."
                    kmEnd.replace(",", ".").toDoubleOrNull() == null && kmEnd.isNotBlank() -> error = "KM final inválido."
                    endValue < startValue -> error = "O KM final não pode ser menor que o KM inicial."
                    else -> {
                        viewModel.saveRecord(previewRecord.copy(date = DateUtils.iso(parsedDate)))
                        error = null
                        Toast.makeText(context, "Registro salvo com sucesso", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("Salvar registro")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    done = ""; canceled = ""; supplies = ""; kmStart = ""; kmEnd = ""; error = null
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Limpar")
            }
            if (state.records.any { it.date == date }) {
                OutlinedButton(
                    onClick = {
                        viewModel.deleteRecord(date)
                        done = ""; canceled = ""; supplies = ""; kmStart = ""; kmEnd = ""
                        Toast.makeText(context, "Registro excluído", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Excluir dia")
                }
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    decimal: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
        ),
        modifier = modifier,
    )
}

private fun formatInput(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else StatsCalculator.formatNumber(value)
