package com.smartnavpill.service

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import kotlin.math.abs

/**
 * Detecta TOQUE DUPLO e TOQUE LONGO sobre a faixa da barra de navegacao,
 * a partir do fluxo de [MotionEvent] recebido pelo overlay.
 *
 * Projetado para NAO disparar em swipes (gesto de Inicio): se o dedo se move
 * alem de [slop], o toque e descartado como "arrasto" e o sistema trata o
 * gesto normalmente. Assim o toque duplo/longo coexiste com a navegacao.
 *
 * Os limiares ([slop], [doubleTapTimeoutMs], [longPressTimeoutMs]) devem ser
 * resolvidos a partir de ViewConfiguration pelo chamador e passados aqui, o que
 * mantem esta classe sem dependencia de Context e facil de testar.
 *
 * @param onDoubleTap chamado quando dois toques rapidos ocorrem no mesmo ponto.
 * @param onLongPress chamado quando o dedo fica parado alem de [longPressTimeoutMs].
 */
class PillGestureDetector(
    private val slop: Int,
    private val doubleTapTimeoutMs: Long,
    private val longPressTimeoutMs: Long,
    private val onDoubleTap: () -> Unit,
    private val onLongPress: () -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())

    private var downX = 0f
    private var downY = 0f
    private var moved = false
    private var longPressFired = false

    // Instante/posicao do ultimo toque "limpo" concluido, para o toque duplo.
    private var lastTapUpTime = 0L
    private var lastTapX = 0f
    private var lastTapY = 0f

    private val longPressRunnable = Runnable {
        if (!moved) {
            longPressFired = true
            onLongPress()
        }
    }

    /**
     * Processa um evento de toque. Retorna true quando o evento pertence a um
     * gesto reconhecido/aguardado (consumido), false para deixar o sistema tratar.
     */
    fun onTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                moved = false
                longPressFired = false
                handler.removeCallbacks(longPressRunnable)
                handler.postDelayed(longPressRunnable, longPressTimeoutMs)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!moved) {
                    val dx = abs(event.rawX - downX)
                    val dy = abs(event.rawY - downY)
                    if (dx > slop || dy > slop) {
                        // Virou arrasto/swipe: cancela long press e ignora como gesto.
                        moved = true
                        handler.removeCallbacks(longPressRunnable)
                    }
                }
                return false
            }

            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressRunnable)
                if (longPressFired || moved) {
                    lastTapUpTime = 0L
                    return longPressFired
                }

                val now = System.currentTimeMillis()
                val withinDoubleTap = (now - lastTapUpTime) <= doubleTapTimeoutMs
                val sameSpot = abs(event.rawX - lastTapX) <= slop * 3 &&
                    abs(event.rawY - lastTapY) <= slop * 3

                return if (withinDoubleTap && sameSpot) {
                    lastTapUpTime = 0L
                    onDoubleTap()
                    true
                } else {
                    // Primeiro toque: registra para aguardar um possivel segundo.
                    lastTapUpTime = now
                    lastTapX = event.rawX
                    lastTapY = event.rawY
                    true
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                moved = false
                longPressFired = false
                return false
            }
        }
        return false
    }

    fun cancel() {
        handler.removeCallbacks(longPressRunnable)
    }
}
