package com.servicofedex.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.servicofedex.app.AppUiState
import com.servicofedex.app.ui.components.AppCard
import com.servicofedex.app.ui.components.HighlightAmountCard
import com.servicofedex.app.ui.components.SectionTitle
import com.servicofedex.app.ui.components.StatLine
import com.servicofedex.app.util.StatsCalculator

@Composable
fun EarningsScreen(state: AppUiState, modifier: Modifier = Modifier) {
    val settings = state.settings
    val day = StatsCalculator.day(state.records, settings)
    val week = StatsCalculator.week(state.records, settings)
    val month = StatsCalculator.month(state.records, settings)
    val max = maxOf(day.amount, week.amount, month.amount, 1.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Ganhos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Valores em reais calculados automaticamente a partir dos registros.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        EarningBar(label = "Hoje", amount = day.amount, fraction = (day.amount / max).toFloat())
        EarningBar(label = "Semana vigente", amount = week.amount, fraction = (week.amount / max).toFloat())
        EarningBar(label = "Mês vigente", amount = month.amount, fraction = (month.amount / max).toFloat())

        HighlightAmountCard(
            label = "Total acumulado no mês",
            amount = StatsCalculator.formatCurrency(month.amount),
            highlighted = true,
            subline = "${month.totalOrders} ordens pagas no mês",
        )

        AppCard {
            SectionTitle("Como o valor é calculado")
            StatLine("Cada ordem de serviço", StatsCalculator.formatCurrency(settings.serviceValue))
            StatLine("Cada suprimento", StatsCalculator.formatCurrency(settings.supplyValue))
            StatLine("Ordens canceladas", "Não entram no cálculo")
            StatLine("Fórmula", "serviços × valor + suprimentos × valor")
        }
    }
}

@Composable
private fun EarningBar(label: String, amount: Double, fraction: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        StatLine(label, StatsCalculator.formatCurrency(amount), emphasize = true)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0.01f, 1f))
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
