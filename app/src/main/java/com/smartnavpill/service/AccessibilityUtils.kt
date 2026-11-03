package com.smartnavpill.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils

/** Helpers para checar/abrir o estado do AccessibilityService do app. */
object AccessibilityUtils {

    /**
     * Retorna true se o NavPillService esta habilitado nas Configuracoes de
     * Acessibilidade. Le a lista do sistema em vez de depender apenas da flag
     * em memoria do servico, para refletir o estado real mesmo apos reboot.
     */
    fun isServiceEnabled(context: Context): Boolean {
        val expected = ComponentName(context, NavPillService::class.java)
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':').apply { setString(enabled) }
        for (entry in splitter) {
            val cn = ComponentName.unflattenFromString(entry)
            if (cn != null && cn == expected) return true
        }
        return false
    }

    /** Abre a tela de Acessibilidade do sistema para o usuario ativar o servico. */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
