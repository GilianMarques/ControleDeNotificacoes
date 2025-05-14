package dev.gmarques.controledenotificacoes.framework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.gmarques.controledenotificacoes.framework.notification_service.NotificationServiceManager

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 14 de maio de 2025 as 12:34.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, NotificationServiceManager::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
