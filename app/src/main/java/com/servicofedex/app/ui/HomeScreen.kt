package com.servicofedex.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.servicofedex.app.AppUiState
import com.servicofedex.app.ui.components.AppCard
import com.servicofedex.app.ui.components.CounterBadge
import com.servicofedex.app.ui.components.HighlightAmountCard
import com.servicofedex.app.ui.components.SectionTitle
import com.servicofedex.app.ui.components.StatLine
import com.servicofedex.app.util.DateUtils
import com.servicofedex.app.util.StatsCalculator

@Composable
fun HomeScreen(state: AppUiState, modifier: Modifier = Modifier) {
    val today = DateUtils.today()
    val settings = state.settings
    val day = StatsCalculator.day(state.records, settings, today)
    val week = StatsCalculator.week(state.records, settings, today)
    val month = StatsCalculator.month(state.records, settings, today)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Olá, ${settings.userName}!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Resumo de hoje, ${DateUtils.formatBr(today)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        HighlightAmountCard(
            label = "A receber hoje",
            amount = StatsCalculator.formatCurrency(day.amount),
            subline = "${day.totalOrders} ordens (${day.servicesDone} serviços + ${day.suppliesDone} suprimentos)",
            highlighted = true,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HighlightAmountCard(
                label = "Semana vigente",
                amount = StatsCalculator.formatCurrency(week.amount),
                subline = "${week.totalOrders} ordens",
                modifier = Modifier.weight(1f),
            )
            HighlightAmountCard(
                label = "Mês vigente",
                amount = StatsCalculator.formatCurrency(month.amount),
                subline = "${month.totalOrders} ordens",
                modifier = Modifier.weight(1f),
            )
        }

        AppCard {
            SectionTitle("Ordens de serviço")
            Row(modifier = Modifier.fillMaxWidth()) {
                CounterBadge(
                    label = "Feitas hoje",
                    value = day.servicesDone.toString(),
                    modifier = Modifier.weight(1f),
                )
                CounterBadge(
                    label = "Canceladas",
                    value = day.servicesCanceled.toString(),
                    modifier = Modifier.weight(1f),
                )
                CounterBadge(
                    label = "Suprimentos",
                    value = day.suppliesDone.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            StatLine("Total de ordens no dia", day.totalOrders.toString(), emphasize = true)
            StatLine("Feitas na semana", week.servicesDone.toString())
            StatLine("Feitas no mês", month.servicesDone.toString())
            StatLine("Canceladas na semana", week.servicesCanceled.toString())
            StatLine("Canceladas no mês", month.servicesCanceled.toString())
        }

        AppCard {
            SectionTitle("Distância percorrida")
            StatLine("KM hoje", "${StatsCalculator.formatNumber(day.kmRun)} km", emphasize = true)
            StatLine("KM na semana", "${StatsCalculator.formatNumber(week.kmRun)} km")
            StatLine("KM no mês", "${StatsCalculator.formatNumber(month.kmRun)} km")
            if (settings.autonomy > 0.0) {
                StatLine(
                    "Consumo estimado no dia",
                    "${StatsCalculator.formatNumber(day.kmRun / settings.autonomy, 2)} L",
                )
            }
            StatLine("Veículo", settings.vehicleName)
        }
    }
}
