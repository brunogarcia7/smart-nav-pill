package com.smartnavpill.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartnavpill.data.AppInfo
import com.smartnavpill.data.AppLauncher
import com.smartnavpill.data.NavPillSettings
import com.smartnavpill.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Qual gesto o usuario esta configurando ao abrir a lista de apps. */
enum class GestureSlot { DOUBLE_TAP, LONG_PRESS }

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = SettingsRepository(app)

    val settings: StateFlow<NavPillSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NavPillSettings())

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _loadingApps = MutableStateFlow(false)
    val loadingApps: StateFlow<Boolean> = _loadingApps.asStateFlow()

    fun setEnabled(value: Boolean) = viewModelScope.launch { repository.setEnabled(value) }

    fun setVibrate(value: Boolean) = viewModelScope.launch { repository.setVibrate(value) }

    fun setWidthPercent(value: Int) = viewModelScope.launch { repository.setWidthPercent(value) }

    fun setCenterOnPill(value: Boolean) = viewModelScope.launch { repository.setCenterOnPill(value) }

    fun setHeightDp(value: Int) = viewModelScope.launch { repository.setHeightDp(value) }

    fun setLongPressMs(value: Int) = viewModelScope.launch { repository.setLongPressMs(value) }

    fun assignApp(slot: GestureSlot, packageName: String?) = viewModelScope.launch {
        when (slot) {
            GestureSlot.DOUBLE_TAP -> repository.setDoubleTapPackage(packageName)
            GestureSlot.LONG_PRESS -> repository.setLongPressPackage(packageName)
        }
    }

    fun labelFor(packageName: String?): String? =
        AppLauncher.labelFor(getApplication(), packageName)

    /** Carrega a lista de apps instalados fora da main thread. */
    fun loadApps() = viewModelScope.launch {
        _loadingApps.value = true
        val list = withContext(Dispatchers.IO) {
            AppLauncher.listLaunchableApps(getApplication())
        }
        _apps.value = list
        _loadingApps.value = false
    }
}
