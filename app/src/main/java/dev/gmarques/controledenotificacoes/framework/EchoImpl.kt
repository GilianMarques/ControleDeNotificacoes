package dev.gmarques.controledenotificacoes.framework

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.domain.framework.Echo
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 02 de julho de 2025 as 09:33.
 *
 * Classe responsavel por republicar as notificações
 */
class EchoImpl @Inject constructor(@ApplicationContext private val baseContext: Context) : Echo {

    /** Republica a notificação se o eco estiver habilitado e a notificação for valida.
     * A notificação é republicada com as mesmas informações da original, mas com um id e tag
     * diferentes para que o sistema a identifique como uma nova notificação.
     * A notificação é cancelada automaticamente apos um segundo.*/
    override fun repostIfNotification(sbn: StatusBarNotification) {

        if (!isEchoEnabled()) return
        if (!isNotificationValid(sbn)) return

        val original = sbn.notification
        val notificationId = sbn.id + 10000
        val notificationTag = sbn.tag

        val notificationManager = baseContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val echoChannel = "echo_channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                echoChannel, baseContext.getString(R.string.Canal_echo), NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val echoedNotification = NotificationCompat.Builder(baseContext, echoChannel).setSmallIcon(R.drawable.vec_echo)
            .setContentTitle(original.extras.getCharSequence(Notification.EXTRA_TITLE))
            .setContentText(original.extras.getCharSequence(Notification.EXTRA_TEXT))
            .setSubText(original.extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
            .setStyle(NotificationCompat.BigTextStyle().bigText(original.extras.getCharSequence(Notification.EXTRA_TEXT)))
            .setWhen(System.currentTimeMillis()).setAutoCancel(true).setGroup("${System.currentTimeMillis()}")
            .setGroupSummary(false).setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

        notificationManager.notify(notificationTag, notificationId, echoedNotification)

        Handler(Looper.getMainLooper()).postDelayed({
            notificationManager.cancel(notificationTag, notificationId)
        }, 1000)

    }

    /** Retorna true se o eco estiver habilitado, false caso contrario */
    private fun isEchoEnabled(): Boolean {
        return !PreferencesImpl.echoEnabled.isDefault()
    }

    /**Retorna true se a notificação for valida, false caso contrario */
    private fun isNotificationValid(sbn: StatusBarNotification): Boolean {
        return !isMediaPlaybackNotification(sbn)
    }

    /**Não reposta notificações de apps de musica ou video
     * Retorna true se a notificação for de um app de midia, false caso contrario */
    private fun isMediaPlaybackNotification(sbn: StatusBarNotification): Boolean {
        // Verifica se há estilo de media (MediaStyle)
        return sbn.notification.extras.getString(Notification.EXTRA_TEMPLATE)?.contains("MediaStyle") == true
    }
}