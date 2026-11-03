package com.smartnavpill.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.smartnavpill.service.AccessibilityUtils

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartNavPillTheme {
                AppRoot(viewModel)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun AppRoot(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pickingSlot by remember { mutableStateOf<GestureSlot?>(null) }

    // Reavalia se o servico de acessibilidade esta ativo sempre que a tela volta
    // ao primeiro plano (o usuario pode ter ativado nas Configuracoes).
    var serviceEnabled by remember { mutableStateOf(AccessibilityUtils.isServiceEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceEnabled = AccessibilityUtils.isServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val slot = pickingSlot
    if (slot == null) {
        MainScreen(
            viewModel = viewModel,
            serviceEnabled = serviceEnabled,
            onOpenAccessibility = { AccessibilityUtils.openAccessibilitySettings(context) },
            onPickApp = { pickingSlot = it }
        )
    } else {
        val apps by viewModel.apps.collectAsStateWithLifecycle()
        val loading by viewModel.loadingApps.collectAsStateWithLifecycle()
        LaunchedEffect(slot) { viewModel.loadApps() }

        val title = when (slot) {
            GestureSlot.DOUBLE_TAP -> "App do toque duplo"
            GestureSlot.LONG_PRESS -> "App do toque longo"
        }
        AppPickerScreen(
            title = title,
            apps = apps,
            loading = loading,
            onPick = { pkg ->
                viewModel.assignApp(slot, pkg)
                pickingSlot = null
            },
            onClear = {
                viewModel.assignApp(slot, null)
                pickingSlot = null
            },
            onBack = { pickingSlot = null }
        )
    }
}
