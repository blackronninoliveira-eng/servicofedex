package com.servicofedex.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistência local simples e sem dependências: SharedPreferences + org.json.
 * Funciona offline e sobrevive ao fechamento do aplicativo.
 */
class PreferencesRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun settings(): AppSettings = AppSettings(
        serviceValue = prefs.getFloat(KEY_SERVICE_VALUE, 16f).toDouble(),
        supplyValue = prefs.getFloat(KEY_SUPPLY_VALUE, 5f).toDouble(),
        vehicleName = prefs.getString(KEY_VEHICLE, "Veículo não informado").orEmpty()
            .ifEmpty { "Veículo não informado" },
        autonomy = prefs.getFloat(KEY_AUTONOMY, 0f).toDouble(),
        userName = prefs.getString(KEY_USER_NAME, "Usuário").orEmpty().ifEmpty { "Usuário" },
        photoPath = prefs.getString(KEY_PHOTO_PATH, null),
        darkTheme = prefs.getBoolean(KEY_DARK_THEME, true),
    )

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putFloat(KEY_SERVICE_VALUE, settings.serviceValue.toFloat())
            .putFloat(KEY_SUPPLY_VALUE, settings.supplyValue.toFloat())
            .putString(KEY_VEHICLE, settings.vehicleName)
            .putFloat(KEY_AUTONOMY, settings.autonomy.toFloat())
            .putString(KEY_USER_NAME, settings.userName)
            .putString(KEY_PHOTO_PATH, settings.photoPath)
            .putBoolean(KEY_DARK_THEME, settings.darkTheme)
            .apply()
    }

    fun records(): List<DailyRecord> {
        val raw = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                DailyRecord(
                    date = obj.getString("date"),
                    servicesDone = obj.optInt("servicesDone", 0),
                    servicesCanceled = obj.optInt("servicesCanceled", 0),
                    suppliesDone = obj.optInt("suppliesDone", 0),
                    kmStart = obj.optDouble("kmStart", 0.0),
                    kmEnd = obj.optDouble("kmEnd", 0.0),
                )
            }.sortedBy { it.date }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** Insere ou atualiza o registro do dia (a data é a chave). */
    fun saveRecord(record: DailyRecord) {
        val updated = records().filterNot { it.date == record.date } + record
        persistRecords(updated.sortedBy { it.date })
    }

    fun deleteRecord(date: String) {
        persistRecords(records().filterNot { it.date == date })
    }

    fun clearRecords() = persistRecords(emptyList())

    private fun persistRecords(list: List<DailyRecord>) {
        val array = JSONArray()
        list.forEach { r ->
            array.put(
                JSONObject()
                    .put("date", r.date)
                    .put("servicesDone", r.servicesDone)
                    .put("servicesCanceled", r.servicesCanceled)
                    .put("suppliesDone", r.suppliesDone)
                    .put("kmStart", r.kmStart)
                    .put("kmEnd", r.kmEnd),
            )
        }
        prefs.edit().putString(KEY_RECORDS, array.toString()).apply()
    }

    private companion object {
        const val PREF_NAME = "servicofedex_prefs"
        const val KEY_SERVICE_VALUE = "service_value"
        const val KEY_SUPPLY_VALUE = "supply_value"
        const val KEY_VEHICLE = "vehicle_name"
        const val KEY_AUTONOMY = "autonomy"
        const val KEY_USER_NAME = "user_name"
        const val KEY_PHOTO_PATH = "photo_path"
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_RECORDS = "records"
    }
}
