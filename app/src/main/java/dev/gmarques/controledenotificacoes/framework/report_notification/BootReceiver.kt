package dev.gmarques.controledenotificacoes.framework.report_notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmsOnBootUseCase
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 14 de maio de 2025 as 12:34.
 *
 *  Liga o [NotificationServiceManager] para que o [dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener]
 *  seja monitorado em caso de desconexão, Também é responsável por, através do [RescheduleAlarmsOnBootUseCase] reagendar os
 *  alarmes que estavam ativos antes do dispositivo reiniciar
 */
class BootReceiver : BroadcastReceiver(), CoroutineScope by MainScope() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAlarmsIfAny()
        }
    }

    private fun rescheduleAlarmsIfAny() {
        val rescheduleAlarmsOnBootUseCase = HiltEntryPoints.rescheduleAlarmsOnBootUseCase()

        launch(IO) { rescheduleAlarmsOnBootUseCase() }

    }


}
