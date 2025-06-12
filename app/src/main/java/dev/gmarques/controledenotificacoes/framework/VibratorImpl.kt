package dev.gmarques.controledenotificacoes.framework

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.framework.VibratorInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Classe responsável por fornecer feedback de interface, como vibração.
 * Suporta APIs abaixo de 26 utilizando o méto_do `vibrate` legada para compatibilidade.
 */
class VibratorImpl @Inject constructor(@ApplicationContext private val context: Context) : VibratorInterface,
    CoroutineScope by MainScope() {

    /**
     * Vibra o dispositivo para fornecer feedback tátil ao usuário em caso de sucesso.
     * Realiza uma vibração de duração moderada.
     */
    override fun error() {
        launch { vibrate(300) }
    }

    /**
     * Vibra o dispositivo para fornecer feedback tátil ao usuário em caso de erro.
     * Realiza cinco vibrações rápidas.
     */
    override fun success() {
        launch {
            repeat(5) {
                vibrate(35)
                delay(85)
            }
        }
    }

    /**
     * Vibra o dispositivo para fornecer feedback tátil ao usuário em caso de interação.
     * Realiza uma vibração curta.
     */
    override fun interaction() {
        launch { vibrate(25) }// Duração curta
    }

    override fun sineAnimation() {
        launch {
            val vib = 50L
            val pause = 400L

            delay(pause / 2)
            repeat(3) {
                vibrate(vib)
                delay(pause)
            }
        }
    }

    /**
     * Vibra o dispositivo para fornecer feedback tátil ao usuário.
     * Utiliza `VibrationEffect` para APIs >= 26 e o méto_do `vibrate` legado para versões anteriores.
     *
     * @param duration A duração da vibração em milissegundos.
     */
    @Suppress("DEPRECATION")
    private fun vibrate(duration: Long) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // API 31 e superior (Android 12+)

            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            val combinedVibration = CombinedVibration.createParallel(vibrationEffect)
            vibratorManager.vibrate(combinedVibration)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  // API 26 a 30 (Android 8 a 11)

            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)

        } else {  // APIs abaixo de 26

            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(duration)
        }
    }

}
