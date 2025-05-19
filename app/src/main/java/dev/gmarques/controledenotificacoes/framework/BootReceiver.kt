package dev.gmarques.controledenotificacoes.framework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import dev.gmarques.controledenotificacoes.di.entry_points.UseCasesEntryPoint
import dev.gmarques.controledenotificacoes.framework.notification_service.NotificationServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 14 de maio de 2025 as 12:34.
 *
 *  Liga o [NotificationServiceManager] para que o [dev.gmarques.controledenotificacoes.framework.notification_service.NotificationListener]
 *  seja monitorado em caso de desconexão
 */
class BootReceiver : BroadcastReceiver(), CoroutineScope by MainScope() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            startNotificationServiceManager(context)
            rescheduleAlarmsIfAny(context)
        }
    }

    private fun rescheduleAlarmsIfAny(context: Context) {
        Log.d("USUK", "BootReceiver.".plus("rescheduleAlarmsIfAny() "))

        val rescheduleAlarmsOnBootUseCase = EntryPointAccessors
            .fromApplication(context, UseCasesEntryPoint::class.java)
            .rescheduleAlarmsOnBootUseCase()

        launch(IO) { rescheduleAlarmsOnBootUseCase() }

    }

    private fun startNotificationServiceManager(context: Context) {
        val serviceIntent = Intent(context, NotificationServiceManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
