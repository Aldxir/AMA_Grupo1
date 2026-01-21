package com.example.alden.animations.state

import android.content.Context
import android.content.SharedPreferences

/**
 * Persistencia de estado relacionado a animaciones (Práctica 11).
 *
 * Se usa SharedPreferences porque ya existe en el proyecto (SessionManager) y no requiere
 * dependencias extra. Si tu docente pide DataStore, esta clase se puede migrar fácilmente.
 */
class AnimStateStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(): AnimState {
        return AnimState(
            lastAction = prefs.getString(KEY_LAST_ACTION, null),
            lastActionTimestamp = prefs.getLong(KEY_LAST_ACTION_TS, 0L),
            lastRegisterSuccess = prefs.getBoolean(KEY_LAST_SUCCESS, false)
        )
    }

    fun writeLastAction(action: String, success: Boolean) {
        prefs.edit()
            .putString(KEY_LAST_ACTION, action)
            .putLong(KEY_LAST_ACTION_TS, System.currentTimeMillis())
            .putBoolean(KEY_LAST_SUCCESS, success)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun addListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun removeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private companion object {
        private const val PREFS_NAME = "anim_state_prefs"
        private const val KEY_LAST_ACTION = "last_action"
        private const val KEY_LAST_ACTION_TS = "last_action_ts"
        private const val KEY_LAST_SUCCESS = "last_success"
    }
}
