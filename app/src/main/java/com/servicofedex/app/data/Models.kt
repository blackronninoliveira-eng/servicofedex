package com.servicofedex.app.data

import kotlin.math.max

/** Uma entrada diária de trabalho. A data é a chave do registro (yyyy-MM-dd). */
data class DailyRecord(
    val date: String,
    val servicesDone: Int,
    val servicesCanceled: Int,
    val suppliesDone: Int,
    val kmStart: Double,
    val kmEnd: Double,
) {
    val kmRun: Double
        get() = max(0.0, kmEnd - kmStart)

    fun totalOrders(): Int = servicesDone + suppliesDone

    fun amount(settings: AppSettings): Double =
        servicesDone * settings.serviceValue + suppliesDone * settings.supplyValue

    fun estimatedLiters(settings: AppSettings): Double? =
        if (settings.autonomy > 0.0) kmRun / settings.autonomy else null
}

/** Preferências editáveis na tela de configurações. */
data class AppSettings(
    val serviceValue: Double = 16.0,
    val supplyValue: Double = 5.0,
    val vehicleName: String = "Veículo não informado",
    val autonomy: Double = 0.0,
    val userName: String = "Usuário",
    val photoPath: String? = null,
    val darkTheme: Boolean = true,
)

/** Somatório genérico de um conjunto de registros. */
data class Totals(
    val servicesDone: Int = 0,
    val servicesCanceled: Int = 0,
    val suppliesDone: Int = 0,
    val kmRun: Double = 0.0,
    val amount: Double = 0.0,
) {
    val totalOrders: Int get() = servicesDone + suppliesDone
}

enum class ExportScope { DAY, WEEK, MONTH }

/** Opção de período mostrada no seletor da tela de histórico. */
data class PeriodOption(
    val key: String,
    val label: String,
    val dates: List<String>,
)
