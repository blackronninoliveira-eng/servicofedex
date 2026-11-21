package com.servicofedex.app.ui

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.servicofedex.app.AppUiState
import com.servicofedex.app.MainViewModel
import com.servicofedex.app.data.ExportScope
import com.servicofedex.app.ui.components.AppCard
import com.servicofedex.app.ui.components.SectionTitle
import com.servicofedex.app.ui.components.StatLine
import com.servicofedex.app.util.DateUtils
import com.servicofedex.app.util.PdfExporter
import com.servicofedex.app.util.PdfShare
import com.servicofedex.app.util.StatsCalculator

private enum class HistoryFilter { ALL, DAY, WEEK, MONTH }

@Composable
fun HistoryScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    var exportScope by remember { mutableStateOf(ExportScope.DAY) }
    var selectedKey by remember { mutableStateOf<String?>(null) }
    var includeKm by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val records = state.records.sortedByDescending { it.date }
    val visible = when (filter) {
        HistoryFilter.ALL -> records
        HistoryFilter.DAY -> records.filter { it.date == DateUtils.iso(DateUtils.today()) }
        HistoryFilter.WEEK -> records.filter { DateUtils.inCurrentWeek(it.date) }
        HistoryFilter.MONTH -> records.filter { DateUtils.inCurrentMonth(it.date) }
    }

    val options = remember(state.records, exportScope) {
        DateUtils.periodOptions(state.records, exportScope)
    }
    LaunchedEffect(exportScope) { selectedKey = options.firstOrNull()?.key }
    val selectedOption = options.find { it.key == selectedKey }
    val exportRecords = state.records.filter { it.date in (selectedOption?.dates ?: emptyList()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Histórico",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        AppCard {
            SectionTitle("Filtrar visualização")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("Todos", filter == HistoryFilter.ALL) { filter = HistoryFilter.ALL }
                Chip("Hoje", filter == HistoryFilter.DAY) { filter = HistoryFilter.DAY }
                Chip("Semana", filter == HistoryFilter.WEEK) { filter = HistoryFilter.WEEK }
                Chip("Mês", filter == HistoryFilter.MONTH) { filter = HistoryFilter.MONTH }
            }
            val totals = when (filter) {
                HistoryFilter.ALL -> StatsCalculator.sum(state.records, state.settings)
                HistoryFilter.DAY -> StatsCalculator.day(state.records, state.settings)
                HistoryFilter.WEEK -> StatsCalculator.week(state.records, state.settings)
                HistoryFilter.MONTH -> StatsCalculator.month(state.records, state.settings)
            }
            StatLine("Ordens no filtro", totals.totalOrders.toString(), emphasize = true)
            StatLine("Valor no filtro", StatsCalculator.formatCurrency(totals.amount), emphasize = true)
            StatLine("KM rodados no filtro", "${StatsCalculator.formatNumber(totals.kmRun)} km")
        }

        AppCard {
            SectionTitle("Exportar PDF")
            Text(
                text = "Escolha o detalhamento e o período:",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("Dia", exportScope == ExportScope.DAY) {
                    exportScope = ExportScope.DAY
                }
                Chip("Semana", exportScope == ExportScope.WEEK) {
                    exportScope = ExportScope.WEEK
                }
                Chip("Mês", exportScope == ExportScope.MONTH) {
                    exportScope = ExportScope.MONTH
                }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { option ->
                    Chip(option.label, option.key == selectedKey) { selectedKey = option.key }
                }
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = includeKm,
                    onCheckedChange = { includeKm = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurface,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
                Text(
                    text = "Incluir KM rodado no PDF",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (message != null) {
                Text(text = message.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val option = selectedOption
                        if (option == null) {
                            message = "Não há períodos com registros para exportar."
                        } else {
                            try {
                                val file = PdfExporter.generate(
                                    context = context,
                                    records = exportRecords,
                                    settings = state.settings,
                                    scope = exportScope,
                                    option = option,
                                    includeKm = includeKm,
                                )
                                message = "PDF gerado: ${file.name}"
                                PdfShare.share(context, file)
                            } catch (e: Exception) {
                                message = "Não foi possível gerar o PDF: ${e.message}"
                            }
                        }
                    },
                    enabled = selectedOption != null,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text("Exportar / compartilhar")
                }
                OutlinedButton(
                    onClick = {
                        val option = selectedOption
                        if (option != null) {
                            try {
                                val file = PdfExporter.generate(
                                    context, exportRecords, state.settings, exportScope, option, includeKm,
                                )
                                PdfShare.view(context, file)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = selectedOption != null,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Visualizar")
                }
            }
        }

        Text(
            text = "Entradas registradas (${visible.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(visible) { record ->
                AppCard {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = DateUtils.formatBr(record.date),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "${record.servicesDone} serviços  •  ${record.servicesCanceled} cancelados  •  ${record.suppliesDone} suprimentos",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = StatsCalculator.formatCurrency(record.amount(state.settings)),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "KM: ${StatsCalculator.formatNumber(record.kmStart)} → " +
                                    "${StatsCalculator.formatNumber(record.kmEnd)} " +
                                    "(${StatsCalculator.formatNumber(record.kmRun)} rodados)",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        IconButton(onClick = { viewModel.deleteRecord(record.date) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir registro",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}
