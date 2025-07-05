package dev.gmarques.controledenotificacoes.framework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints

/**
 * Criado por Gilian Marques
 * Em 05/07/2025 as 19:22
 *
 * responsavel por, de tempos em tempos, ligar o serviço caso seja fechado por erro ou sistema
 */
class AutoTurnOnReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // App.instance.startNotificationService() // nao é necessario fazer a chamada pq a classe app ja faz isso ao ser aberta.
        HiltEntryPoints.scheduleAutoTurnOnUseCase().invoke()
    }

}