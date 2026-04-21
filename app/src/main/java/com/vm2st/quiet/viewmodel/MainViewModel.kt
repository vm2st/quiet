package com.vm2st.quiet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vm2st.quiet.data.AppDatabase
import com.vm2st.quiet.data.Ritual
import com.vm2st.quiet.worker.CheckWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") application: Application, // ← Подавляем предупреждение
    private val database: AppDatabase
) : AndroidViewModel(application) {

    private val ritualDao = database.ritualDao()

    val rituals = ritualDao.getAllRituals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addRitual(name: String) {
        viewModelScope.launch {
            ritualDao.insert(Ritual(name = name))
            // Явно приводим Application к Context (не обязательно, но убирает варнинг)
            CheckWorker.schedule(getApplication<Application>().applicationContext)
        }
    }

    fun confirmRitual(ritual: Ritual) {
        viewModelScope.launch {
            ritualDao.update(ritual.copy(isConfirmedToday = true))
        }
    }

    fun deleteRitual(ritual: Ritual) {
        viewModelScope.launch {
            ritualDao.delete(ritual)
        }
    }

    // Функция будет использоваться в MainActivity при старте
    fun resetDailyConfirmations() {
        viewModelScope.launch {
            ritualDao.resetAllConfirmations()
        }
    }
    fun toggleConfirmRitual(ritual: Ritual) {
        viewModelScope.launch {
            val newConfirmed = !ritual.isConfirmedToday
            val updated = ritual.copy(
                isConfirmedToday = newConfirmed,
                lastConfirmedTime = if (newConfirmed) System.currentTimeMillis() else ritual.lastConfirmedTime
            )
            ritualDao.update(updated)
            if (newConfirmed) {
                // Запускаем проверку через 5 минут
                CheckWorker.scheduleFiveMinuteCheck(getApplication(), ritual.id)
                // Также можно запустить основную проверку через 1-4 часа
                CheckWorker.schedule(getApplication())
            }
        }
    }
}