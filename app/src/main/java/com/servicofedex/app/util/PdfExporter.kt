package com.servicofedex.app.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.servicofedex.app.data.AppSettings
import com.servicofedex.app.data.DailyRecord
import com.servicofedex.app.data.ExportScope
import com.servicofedex.app.data.PeriodOption
import com.servicofedex.app.ui.theme.Dark
import com.servicofedex.app.ui.theme.Yellow
import com.servicofedex.app.ui.theme.YellowDeep
import androidx.compose.ui.graphics.toArgb
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Geração de PDF 100% nativa (android.graphics.pdf.PdfDocument), sem bibliotecas
 * de terceiros. Todas as cores usadas no documento vêm da paleta obrigatória.
 */
object PdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val LEFT = 36f
    private const val RIGHT = 559f
    private const val ROW_HEIGHT = 24f

    private val darkArgb = Dark.toArgb()
    private val yellowArgb = Yellow.toArgb()
    private val yellowDeepArgb = YellowDeep.toArgb()

    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color
    }

    private fun text(size: Float, color: Int, bold: Boolean = false) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

    fun generate(
        context: Context,
        records: List<DailyRecord>,
        settings: AppSettings,
        scope: ExportScope,
        option: PeriodOption,
        includeKm: Boolean,
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()

        var pageNumber = 1
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = 0f

        fun background() {
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), fill(yellowDeepArgb))
        }

        fun finishPage() {
            canvas.drawText(
                "Página $pageNumber",
                RIGHT - 70f,
                PAGE_HEIGHT - 22f,
                text(9f, darkArgb),
            )
            document.finishPage(page)
        }

        fun drawTableHeader() {
            canvas.drawRect(LEFT, y, RIGHT, y + 26f, fill(darkArgb))
            val label = text(10f, yellowArgb, bold = true)
            canvas.drawText("Data", 44f, y + 17f, label)
            canvas.drawText("Feitas", 176f, y + 17f, label)
            canvas.drawText("Cancel.", 232f, y + 17f, label)
            canvas.drawText("Supr.", 296f, y + 17f, label)
            canvas.drawText("Valor (R$)", 360f, y + 17f, label)
            if (includeKm) canvas.drawText("KM", 504f, y + 17f, label)
            y += 34f
        }

        fun newPage() {
            finishPage()
            pageNumber++
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = 52f
            background()
            drawTableHeader()
        }

        // 1ª página: cabeçalho, resumo e início da tabela.
        background()
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 104f, fill(darkArgb))
        canvas.drawText("ServicoFedex", LEFT, 40f, text(22f, yellowArgb, bold = true))
        val scopeName = when (scope) {
            ExportScope.DAY -> "Dia"
            ExportScope.WEEK -> "Semana"
            ExportScope.MONTH -> "Mês"
        }
        canvas.drawText("Relatório por $scopeName", LEFT, 64f, text(12f, yellowDeepArgb))
        canvas.drawText("Período: ${option.label}", LEFT, 84f, text(11f, yellowDeepArgb))

        y = 122f
        val totals = StatsCalculator.sum(records, settings)
        canvas.drawRect(LEFT, y, RIGHT, y + 92f, fill(yellowArgb))
        val summary = text(12f, darkArgb)
        val summaryBold = text(14f, darkArgb, bold = true)
        canvas.drawText("Total de ordens: ${totals.totalOrders}", 48f, y + 22f, summaryBold)
        canvas.drawText(
            "Feitas: ${totals.servicesDone}    Canceladas: ${totals.servicesCanceled}    Suprimentos: ${totals.suppliesDone}",
            48f,
            y + 44f,
            summary,
        )
        canvas.drawText("Valor a receber: ${StatsCalculator.formatCurrency(totals.amount)}", 48f, y + 66f, summaryBold)
        if (includeKm) {
            val liters = if (settings.autonomy > 0.0) totals.kmRun / settings.autonomy else null
            val consumption = liters?.let { " | Consumo estimado: ${StatsCalculator.formatNumber(it, 2)} L" } ?: ""
            canvas.drawText(
                "KM rodado: ${StatsCalculator.formatNumber(totals.kmRun)} km$consumption",
                48f,
                y + 86f,
                summary,
            )
        }
        y += 108f
        drawTableHeader()

        // 2. Linhas de detalhamento.
        val rowText = text(10f, darkArgb)
        records.forEachIndexed { index, record ->
            if (y + ROW_HEIGHT > PAGE_HEIGHT - 60f) newPage()
            // Faixas alternadas usam apenas as duas cores claras da paleta.
            canvas.drawRect(
                LEFT,
                y,
                RIGHT,
                y + ROW_HEIGHT,
                fill(if (index % 2 == 0) yellowArgb else yellowDeepArgb),
            )
            val base = y + 16f
            canvas.drawText(DateUtils.formatBr(record.date), 44f, base, rowText)
            canvas.drawText(record.servicesDone.toString(), 180f, base, rowText)
            canvas.drawText(record.servicesCanceled.toString(), 240f, base, rowText)
            canvas.drawText(record.suppliesDone.toString(), 304f, base, rowText)
            canvas.drawText(StatsCalculator.formatCurrency(record.amount(settings)), 360f, base, rowText)
            if (includeKm) {
                canvas.drawText("${StatsCalculator.formatNumber(record.kmRun)} km", 504f, base, rowText)
            }
            y += ROW_HEIGHT
        }

        // 3. Rodapé com totalizadores.
        if (y + 42f > PAGE_HEIGHT - 60f) newPage()
        canvas.drawRect(LEFT, y, RIGHT, y + 30f, fill(darkArgb))
        val totalText = text(11f, yellowArgb, bold = true)
        canvas.drawText("TOTAL", 44f, y + 20f, totalText)
        canvas.drawText(totals.servicesDone.toString(), 180f, y + 20f, totalText)
        canvas.drawText(totals.servicesCanceled.toString(), 240f, y + 20f, totalText)
        canvas.drawText(totals.suppliesDone.toString(), 304f, y + 20f, totalText)
        canvas.drawText(StatsCalculator.formatCurrency(totals.amount), 360f, y + 20f, totalText)
        if (includeKm) canvas.drawText("${StatsCalculator.formatNumber(totals.kmRun)} km", 504f, y + 20f, totalText)
        y += 48f

        val note = text(9f, darkArgb)
        canvas.drawText(
            "Valores unitários: ordem ${StatsCalculator.formatCurrency(settings.serviceValue)} | suprimento ${StatsCalculator.formatCurrency(settings.supplyValue)}",
            LEFT,
            y + 12f,
            note,
        )
        canvas.drawText(
            "Gerado em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            LEFT,
            y + 28f,
            note,
        )

        finishPage()

        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeKey = option.key.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(outputDir, "relatorio-${scope.name.lowercase()}-$safeKey.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}
