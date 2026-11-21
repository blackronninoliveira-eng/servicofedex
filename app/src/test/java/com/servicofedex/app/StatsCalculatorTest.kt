package com.servicofedex.app

import com.servicofedex.app.data.AppSettings
import com.servicofedex.app.data.DailyRecord
import com.servicofedex.app.data.ExportScope
import com.servicofedex.app.util.DateUtils
import com.servicofedex.app.util.StatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StatsCalculatorTest {

    private val settings = AppSettings(serviceValue = 16.0, supplyValue = 5.0)

    @Test
    fun `calcula total de ordens e valorUnitario`() {
        val record = DailyRecord("2026-11-21", 3, 1, 2, 100.0, 130.0)
        assertEquals(5, record.totalOrders())
        assertEquals(58.0, record.amount(settings), 0.0001)
        assertEquals(30.0, record.kmRun, 0.0001)
    }

    @Test
    fun `km final menor que inicial resulta em zero`() {
        val record = DailyRecord("2026-11-21", 0, 0, 0, 90.0, 80.0)
        assertEquals(0.0, record.kmRun, 0.0001)
    }

    @Test
    fun `agrega por dia semana e mes vigentes`() {
        val reference = LocalDate.of(2026, 11, 21) // sábado; semana: 16 a 22/11
        val records = listOf(
            DailyRecord("2026-11-21", 2, 0, 1, 0.0, 20.0),   // hoje, semana, mês
            DailyRecord("2026-11-16", 1, 1, 0, 0.0, 10.0),   // semana, mês
            DailyRecord("2026-11-02", 4, 0, 0, 0.0, 40.0),   // mês, outra semana
            DailyRecord("2026-10-30", 9, 0, 0, 0.0, 90.0),   // fora do mês
        )

        assertEquals(37.0, StatsCalculator.day(records, settings, reference).amount, 0.0001)
        assertEquals(53.0, StatsCalculator.week(records, settings, reference).amount, 0.0001)
        assertEquals(117.0, StatsCalculator.month(records, settings, reference).amount, 0.0001)
        assertEquals(70.0, StatsCalculator.month(records, settings, reference).kmRun, 0.0001)
    }

    @Test
    fun `opcoes de periodo por semana e mes`() {
        val records = listOf(
            DailyRecord("2026-11-21", 0, 0, 0, 0.0, 0.0),
            DailyRecord("2026-11-16", 0, 0, 0, 0.0, 0.0),
            DailyRecord("2026-10-30", 0, 0, 0, 0.0, 0.0),
        )
        assertEquals(3, DateUtils.periodOptions(records, ExportScope.DAY).size)
        assertEquals(2, DateUtils.periodOptions(records, ExportScope.WEEK).size)
        assertEquals(2, DateUtils.periodOptions(records, ExportScope.MONTH).size)
    }
}
