package com.servicofedex.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.servicofedex.app.data.AppSettings
import com.servicofedex.app.data.DailyRecord
import com.servicofedex.app.data.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppUiState(
    val settings: AppSettings = AppSettings(),
    val records: List<DailyRecord> = emptyList(),
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository(application)
    private val _uiState = MutableStateFlow(
        AppUiState(repository.settings(), repository.records()),
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun saveRecord(record: DailyRecord) {
        repository.saveRecord(record)
        refresh()
    }

    fun deleteRecord(date: String) {
        repository.deleteRecord(date)
        refresh()
    }

    fun clearRecords() {
        repository.clearRecords()
        refresh()
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_uiState.value.settings)
        repository.saveSettings(updated)
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(settings = updated))
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.emit(
                AppUiState(repository.settings(), repository.records()),
            )
        }
    }
}
