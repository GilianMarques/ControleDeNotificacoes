package dev.gmarques.controledenotificacoes.framework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import kotlinx.coroutines.runBlocking

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 04 de julho de 2025 as 11:13.
 *
 * É aberto de tempos em tempos pelo sistema (via agendamento de alarme) para iniciar o serviço caso tenha sido fechado
 * @see dev.gmarques.controledenotificacoes.domain.usecase.alarms.CancelAutoTurnOnUseCase
 * @see dev.gmarques.controledenotificacoes.domain.usecase.alarms.ScheduleAutoTurnOnUseCase
 */
class AutoTurnOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("USUK", "AutoTurnOnReceiver.onReceive: ")
        if (context == null || intent == null) return
        App.instance.startNotificationService()
        scheduleAutoTurnOn()
    }


    private fun scheduleAutoTurnOn() = runBlocking {
        val scheduleAutoBootUseCase = HiltEntryPoints.scheduleAutoTurnOnUseCase()
        scheduleAutoBootUseCase()
    }
}