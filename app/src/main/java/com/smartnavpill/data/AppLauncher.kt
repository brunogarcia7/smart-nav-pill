package com.smartnavpill.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

/** Utilitarios para listar apps lancaveis e abrir um app pelo seu package. */
object AppLauncher {

    private const val TAG = "SmartNavPill"

    /**
     * Lista os apps que possuem um launcher (aparecem na gaveta de apps),
     * ordenados por nome. Executar fora da main thread (consulta pesada).
     */
    fun listLaunchableApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        return resolveInfos
            .mapNotNull { ri ->
                val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
                // Ignora o proprio app na lista de alvos.
                if (pkg == context.packageName) return@mapNotNull null
                val label = ri.loadLabel(pm)?.toString() ?: pkg
                val icon = runCatching { ri.loadIcon(pm) }.getOrNull()
                AppInfo(packageName = pkg, label = label, icon = icon)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    /** Resolve o nome exibivel de um package (para mostrar o app escolhido). */
    fun labelFor(context: Context, packageName: String?): String? {
        if (packageName == null) return null
        return runCatching {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(ai).toString()
        }.getOrNull()
    }

    /**
     * Abre o app indicado trazendo-o para frente. Retorna true se conseguiu.
     * Usa a launch intent padrao do package.
     */
    fun launch(context: Context, packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        val pm = context.packageManager
        val launch = pm.getLaunchIntentForPackage(packageName)
        if (launch == null) {
            Log.w(TAG, "Sem launch intent para $packageName")
            return false
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return try {
            context.startActivity(launch)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao abrir $packageName: ${e.message}", e)
            false
        }
    }
}
