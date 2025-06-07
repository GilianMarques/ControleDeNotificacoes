package dev.gmarques.controledenotificacoes.framework.report_notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Criado por Gilian Marques
 * Em sábado, 07 de junho de 2025 as 17:43.
 */
class ReportNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_ORIGINAL_INTENT = "original_intent"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val originalIntent = intent.getParcelableExtra<Intent>(EXTRA_ORIGINAL_INTENT)

        if (notificationId != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }

        if (originalIntent != null) {
            originalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(originalIntent)
        }
    }
}