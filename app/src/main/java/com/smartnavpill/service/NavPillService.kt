package com.smartnavpill.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import com.smartnavpill.data.AppLauncher
import com.smartnavpill.data.NavPillSettings
import com.smartnavpill.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Servico central do Smart Nav Pill.
 *
 * Mantem um overlay invisivel do tipo TYPE_ACCESSIBILITY_OVERLAY sobre a area da
 * barra de navegacao (gesture pill), na borda inferior da tela, e detecta toque
 * duplo e toque longo nessa regiao para abrir os apps configurados.
 *
 * A tecnica (TYPE_ACCESSIBILITY_OVERLAY + FLAG_NOT_FOCUSABLE) foi validada por
 * prova de conceito no Motorola Edge 30 Ultra (Android 15, gestos): captura o
 * toque na pill sem root, sem SYSTEM_ALERT_WINDOW e sem quebrar os gestos do
 * sistema (swipes continuam indo para o System UI).
 */
class NavPillService : AccessibilityService() {

    companion object {
        private const val TAG = "SmartNavPill"

        @Volatile
        var isRunning: Boolean = false
            private set
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: SettingsRepository

    private var windowManager: WindowManager? = null
    private var overlay: FrameLayout? = null
    private var detector: PillGestureDetector? = null

    private var currentSettings: NavPillSettings = NavPillSettings()

    /** Largura (dp) da faixa no modo "centralizar na barrinha": cobre a pill. */
    private val pillWidthDp = 140

    @OptIn(FlowPreview::class)
    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        repository = SettingsRepository(applicationContext)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Reage a mudancas nas preferencias. O slider emite muitos valores por
        // segundo; aplicar cada um no WindowManager trava a UI. O debounce
        // agrupa a rajada e so reaplica o overlay quando o usuario para de
        // arrastar, mantendo o app responsivo.
        repository.settings
            .distinctUntilChanged()
            .debounce { settings ->
                // Liga/desliga responde na hora; mudancas de tamanho esperam.
                if (settings.enabled == currentSettings.enabled) 120L else 0L
            }
            .onEach { settings ->
                val previous = currentSettings
                currentSettings = settings
                if (settings.enabled) {
                    val longPressChanged = settings.longPressMs != previous.longPressMs
                    addOrUpdateOverlay(settings, recreateDetector = longPressChanged || detector == null)
                } else {
                    removeOverlay()
                }
            }
            .launchIn(scope)

        Log.i(TAG, "Servico conectado.")
    }

    private fun buildDetector(settings: NavPillSettings): PillGestureDetector {
        val vc = ViewConfiguration.get(this)
        return PillGestureDetector(
            slop = vc.scaledTouchSlop,
            doubleTapTimeoutMs = ViewConfiguration.getDoubleTapTimeout().toLong(),
            longPressTimeoutMs = settings.longPressMs.toLong(),
            onDoubleTap = {
                Log.i(TAG, "Toque duplo detectado.")
                vibrate()
                AppLauncher.launch(this, currentSettings.doubleTapPackage)
            },
            onLongPress = {
                Log.i(TAG, "Toque longo detectado.")
                vibrate()
                AppLauncher.launch(this, currentSettings.longPressPackage)
            },
        )
    }

    private fun buildLayoutParams(settings: NavPillSettings): WindowManager.LayoutParams {
        val metrics: DisplayMetrics = resources.displayMetrics
        val density = metrics.density
        val screenWidth = metrics.widthPixels

        val heightPx = (settings.heightDp * density).toInt().coerceAtLeast(1)
        val widthPx = if (settings.centerOnPill) {
            (pillWidthDp * density).toInt().coerceIn(1, screenWidth)
        } else {
            (screenWidth * settings.widthPercent / 100).coerceIn(1, screenWidth)
        }

        return WindowManager.LayoutParams(
            widthPx,
            heightPx,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            // NOT_FOCUSABLE: nao rouba foco do app em primeiro plano.
            // NOT_TOUCH_MODAL + LAYOUT_IN_SCREEN: a janela recebe o toque dentro
            //   dos seus limites e deixa passar o resto, o que faz o overlay
            //   capturar o toque na faixa mesmo com outro app em primeiro plano
            //   (nao so no launcher), sem bloquear os gestos do sistema.
            (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
    }

    /**
     * Adiciona o overlay (se ainda nao existe) ou so atualiza o layout.
     * O detector e recriado apenas quando [recreateDetector] e true (quando o
     * tempo de toque longo muda), evitando trabalho a cada quadro do slider.
     */
    private fun addOrUpdateOverlay(settings: NavPillSettings, recreateDetector: Boolean) {
        val wm = windowManager ?: return

        if (recreateDetector || detector == null) {
            detector?.cancel()
            detector = buildDetector(settings)
        }
        val det = detector ?: return
        val lp = buildLayoutParams(settings)

        val existing = overlay
        if (existing != null && existing.isAttachedToWindow) {
            existing.setOnTouchListener { _, event -> det.onTouch(event) }
            runCatching { wm.updateViewLayout(existing, lp) }
                .onFailure { Log.e(TAG, "Falha ao atualizar overlay: ${it.message}") }
            return
        }

        val view = FrameLayout(this)
        view.setBackgroundColor(0x00000000) // invisivel
        view.setOnTouchListener { _, event -> det.onTouch(event) }

        runCatching {
            wm.addView(view, lp)
            overlay = view
            Log.i(TAG, "Overlay adicionado: center=${settings.centerOnPill}")
        }.onFailure {
            Log.e(TAG, "Falha ao adicionar overlay: ${it.message}", it)
        }
    }

    private fun removeOverlay() {
        detector?.cancel()
        detector = null
        val wm = windowManager
        overlay?.let { v ->
            runCatching { if (v.isAttachedToWindow) wm?.removeViewImmediate(v) }
        }
        overlay = null
    }

    private fun vibrate() {
        if (!currentSettings.vibrate) return
        runCatching {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Quando o app em primeiro plano muda, reafirmamos o overlay no topo da
        // sua camada. Isso corrige o caso em que o toque so funcionava no
        // launcher: ao abrir outro app, reencaixamos o overlay para ele voltar
        // a receber o toque na faixa.
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            currentSettings.enabled
        ) {
            reassertOverlay()
        }
    }

    private var lastReassert = 0L

    /** Reposiciona o overlay no topo (throttled para nao pesar). */
    private fun reassertOverlay() {
        val now = System.currentTimeMillis()
        if (now - lastReassert < 300) return
        lastReassert = now

        val wm = windowManager ?: return
        val v = overlay ?: return
        if (!v.isAttachedToWindow) return
        // Remover e re-adicionar coloca a janela no topo da camada de overlays
        // de acessibilidade, garantindo prioridade de toque sobre o novo app.
        runCatching {
            wm.removeViewImmediate(v)
            wm.addView(v, buildLayoutParams(currentSettings))
        }.onFailure { Log.e(TAG, "Falha ao reafirmar overlay: ${it.message}") }
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        Log.i(TAG, "Servico desvinculado.")
        removeOverlay()
        isRunning = false
        scope.cancel()
        return super.onUnbind(intent)
    }
}
