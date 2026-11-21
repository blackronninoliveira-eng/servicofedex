package com.servicofedex.app.util

import com.servicofedex.app.data.DailyRecord
import com.servicofedex.app.data.ExportScope
import com.servicofedex.app.data.PeriodOption
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

private val ptBR = Locale("pt", "BR")
private val brDate = DateTimeFormatter.ofPattern("dd/MM/yyyy", ptBR)
private val brMonth = DateTimeFormatter.ofPattern("MMMM/yyyy", ptBR)

object DateUtils {
    fun today(): LocalDate = LocalDate.now()

    fun iso(date: LocalDate): String = date.toString()

    fun parse(iso: String): LocalDate? = try {
        LocalDate.parse(iso)
    } catch (_: Exception) {
        null
    }

    fun formatBr(date: LocalDate): String = brDate.format(date)

    fun formatBr(iso: String): String = parse(iso)?.let { brDate.format(it) } ?: iso

    fun formatMonth(date: LocalDate): String =
        brMonth.format(date).replaceFirstChar { it.uppercase(ptBR) }

    fun weekStart(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

    fun weekEnd(date: LocalDate): LocalDate = date.with(DayOfWeek.SUNDAY)

    fun weekKey(date: LocalDate): String = iso(weekStart(date))

    fun monthKey(date: LocalDate): String = YearMonth.from(date).toString()

    fun weekLabel(date: LocalDate): String =
        "${formatBr(weekStart(date))} a ${formatBr(weekEnd(date))}"

    fun weekNumber(date: LocalDate): Int = date.get(WeekFields.ISO.weekOfWeekBasedYear())

    fun inCurrentWeek(isoDate: String, reference: LocalDate = today()): Boolean {
        val d = parse(isoDate) ?: return false
        return d in weekStart(reference)..weekEnd(reference)
    }

    fun inCurrentMonth(isoDate: String, reference: LocalDate = today()): Boolean {
        val d = parse(isoDate) ?: return false
        return YearMonth.from(d) == YearMonth.from(reference)
    }

    /** Monta as opções de período disponíveis para exportação, a partir dos registros. */
    fun periodOptions(records: List<DailyRecord>, scope: ExportScope): List<PeriodOption> {
        val dates = records.mapNotNull { parse(it.date) }.sorted()
        return when (scope) {
            ExportScope.DAY -> dates.map { date ->
                val key = iso(date)
                PeriodOption(key, formatBr(date), listOf(key))
            }

            ExportScope.WEEK -> dates
                .groupBy { weekStart(it) }
                .toSortedMap()
                .map { (start, days) ->
                    PeriodOption(
                        key = iso(start),
                        label = weekLabel(start),
                        dates = days.map { iso(it) },
                    )
                }

            ExportScope.MONTH -> dates
                .groupBy { YearMonth.from(it) }
                .toSortedMap()
                .map { (month, days) ->
                    val representative = month.atDay(1)
                    PeriodOption(
                        key = month.toString(),
                        label = brMonth.format(representative).replaceFirstChar { it.uppercase(ptBR) },
                        dates = days.map { iso(it) },
                    )
                }
        }.reversed()
    }
}
