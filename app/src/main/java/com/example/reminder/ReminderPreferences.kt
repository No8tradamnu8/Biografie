package com.example.reminder

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReminderSettings(
    val isEnabled: Boolean = true,
    val hour: Int = 20,
    val minute: Int = 0
)

class ReminderPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ReminderSettings> = _settings.asStateFlow()

    private fun loadSettings(): ReminderSettings {
        val isEnabled = prefs.getBoolean(KEY_ENABLED, true)
        val hour = prefs.getInt(KEY_HOUR, 20)
        val minute = prefs.getInt(KEY_MINUTE, 0)
        return ReminderSettings(isEnabled, hour, minute)
    }

    fun saveSettings(isEnabled: Boolean, hour: Int, minute: Int) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, isEnabled)
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()
        _settings.value = ReminderSettings(isEnabled, hour, minute)
    }

    fun getSettings(): ReminderSettings = _settings.value

    companion object {
        private const val PREFS_NAME = "tagebuch_reminder_prefs"
        private const val KEY_ENABLED = "reminder_enabled"
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"
    }
}
