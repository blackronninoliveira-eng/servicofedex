package com.servicofedex.app.util

import com.servicofedex.app.data.AppSettings
import com.servicofedex.app.data.DailyRecord
import com.servicofedex.app.data.Totals
import java.time.LocalDate

/** Cálculos de dia, semana e mês vigentes. */
object StatsCalculator {

    fun sum(records: List<DailyRecord>, settings: AppSettings): Totals = Totals(
        servicesDone = records.sumOf { it.servicesDone },
        servicesCanceled = records.sumOf { it.servicesCanceled },
        suppliesDone = records.sumOf { it.suppliesDone },
        kmRun = records.sumOf { it.kmRun },
        amount = records.sumOf { it.amount(settings) },
    )

    fun day(records: List<DailyRecord>, settings: AppSettings, date: LocalDate = DateUtils.today()): Totals =
        sum(records.filter { it.date == DateUtils.iso(date) }, settings)

    fun week(records: List<DailyRecord>, settings: AppSettings, date: LocalDate = DateUtils.today()): Totals =
        sum(records.filter { DateUtils.inCurrentWeek(it.date, date) }, settings)

    fun month(records: List<DailyRecord>, settings: AppSettings, date: LocalDate = DateUtils.today()): Totals =
        sum(records.filter { DateUtils.inCurrentMonth(it.date, date) }, settings)

    fun formatCurrency(value: Double): String =
        "R$ " + String.format(java.util.Locale("pt", "BR"), "%,.2f", value)

    fun formatNumber(value: Double, decimals: Int = 1): String =
        String.format(java.util.Locale("pt", "BR"), "%,.${decimals}f", value)
}
