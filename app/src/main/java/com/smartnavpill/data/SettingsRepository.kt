package com.smartnavpill.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Extensao de Context que cria/obtem o DataStore de preferencias unico do app. */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smart_nav_pill_settings")

/**
 * Estado completo das preferencias do usuario, exposto como um objeto imutavel.
 *
 * @param enabled recurso ligado/desligado.
 * @param doubleTapPackage package do app aberto no toque duplo (ou null).
 * @param longPressPackage package do app aberto no toque longo (ou null).
 * @param widthPercent largura da faixa sensivel, em % da largura da tela (10..100).
 *                     Usado apenas quando [centerOnPill] e false.
 * @param centerOnPill quando true, a faixa fica centralizada e estreita, cobrindo
 *                     so a area da barrinha de inicio (gesture pill), ignorando
 *                     [widthPercent]. E o modo recomendado.
 * @param heightDp altura da faixa sensivel, em dp (a partir da borda inferior).
 * @param longPressMs tempo (ms) para o toque longo disparar (200..800).
 * @param vibrate feedback tatil ao disparar um gesto.
 */
data class NavPillSettings(
    val enabled: Boolean = false,
    val doubleTapPackage: String? = null,
    val longPressPackage: String? = null,
    val widthPercent: Int = 60,
    val centerOnPill: Boolean = true,
    val heightDp: Int = 28,
    val longPressMs: Int = 300,
    val vibrate: Boolean = true,
)

/** Fonte unica de verdade para as preferencias, persistidas via DataStore. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
        val DOUBLE_TAP_PKG = stringPreferencesKey("double_tap_pkg")
        val LONG_PRESS_PKG = stringPreferencesKey("long_press_pkg")
        val WIDTH_PERCENT = intPreferencesKey("width_percent")
        val CENTER_ON_PILL = booleanPreferencesKey("center_on_pill")
        val HEIGHT_DP = intPreferencesKey("height_dp")
        val LONG_PRESS_MS = intPreferencesKey("long_press_ms")
        val VIBRATE = booleanPreferencesKey("vibrate")
    }

    val settings: Flow<NavPillSettings> = context.dataStore.data.map { prefs ->
        NavPillSettings(
            enabled = prefs[Keys.ENABLED] ?: false,
            doubleTapPackage = prefs[Keys.DOUBLE_TAP_PKG],
            longPressPackage = prefs[Keys.LONG_PRESS_PKG],
            widthPercent = (prefs[Keys.WIDTH_PERCENT] ?: 60).coerceIn(10, 100),
            centerOnPill = prefs[Keys.CENTER_ON_PILL] ?: true,
            heightDp = (prefs[Keys.HEIGHT_DP] ?: 28).coerceIn(12, 64),
            longPressMs = (prefs[Keys.LONG_PRESS_MS] ?: 300).coerceIn(200, 800),
            vibrate = prefs[Keys.VIBRATE] ?: true,
        )
    }

    suspend fun setEnabled(value: Boolean) = edit { it[Keys.ENABLED] = value }

    suspend fun setDoubleTapPackage(pkg: String?) = edit {
        if (pkg == null) it.remove(Keys.DOUBLE_TAP_PKG) else it[Keys.DOUBLE_TAP_PKG] = pkg
    }

    suspend fun setLongPressPackage(pkg: String?) = edit {
        if (pkg == null) it.remove(Keys.LONG_PRESS_PKG) else it[Keys.LONG_PRESS_PKG] = pkg
    }

    suspend fun setWidthPercent(value: Int) = edit { it[Keys.WIDTH_PERCENT] = value.coerceIn(10, 100) }

    suspend fun setCenterOnPill(value: Boolean) = edit { it[Keys.CENTER_ON_PILL] = value }

    suspend fun setHeightDp(value: Int) = edit { it[Keys.HEIGHT_DP] = value.coerceIn(12, 64) }

    suspend fun setLongPressMs(value: Int) = edit { it[Keys.LONG_PRESS_MS] = value.coerceIn(200, 800) }

    suspend fun setVibrate(value: Boolean) = edit { it[Keys.VIBRATE] = value }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
