package dev.gmarques.controledenotificacoes.framework.notification_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.gmarques.controledenotificacoes.R
import java.util.Timer
import java.util.TimerTask

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 09:07.
 */
class NotificationServiceManager : Service() {

    private val checkIntervalMs = 10_000L // intervalo entre checagens
    private var timer: Timer? = null
    private val channelId = "notification_watcher_channel"


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        keepCheckingNotificationListenerIsAlive()
        return START_STICKY.also {
            Log.d("USUK", "NotificationServiceManager.onStartCommand: Gestor de serviço conectado")
        }
    }

    private fun keepCheckingNotificationListenerIsAlive() {
        timer?.cancel()
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    forceReconnectNotificationListener()
                }
            }, 0, checkIntervalMs)
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelName = getString(R.string.Monitoramento_de_notificacoes)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.Monitoramento_de_notificacoes))
            .setContentText(getString(R.string.Toque_aqui_para_desativar_esta_notifica_o))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(getPendingIntentForNotificationSettings())
            .build()
    }

    /**
     * Verifica se o listener de notificações está ativo.
     * Essa função pode retornar true por engano em casos onde o usuário mata o aplicativo. Ao abrir ele em seguida essa função vai
     * entender que o listener está ativo mesmo que não esteja, retornando um falso positivo
     */
    @Suppress("unused")
    fun isNotificationListenerActive(): Boolean {
        val cn = ComponentName(baseContext, NotificationListener::class.java)
        val enabledListeners = Settings.Secure.getString(
            baseContext.contentResolver,
            "enabled_notification_listeners"
        )
        return (enabledListeners?.contains(cn.flattenToString()) == true).also {
            Log.d(
                "USUK",
                "NotificationServiceManager.isNotificationListenerActive: $it "
            )
        }
    }

    fun forceReconnectNotificationListener() {
        //     Log.d("USUK", "NotificationServiceManager.forceReconnectNotificationService: tentado ligar listener")

        val pm = packageManager
        val componentName = ComponentName(this, NotificationListener::class.java)

        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * Abre as configurações de notificação para o canal específico da notificação em primeiro plano (foreground).
     * Em versões mais recentes do Android (O+), navega diretamente para as configurações do canal.
     * Em versões anteriores (N e N-MR1), abre as configurações gerais do aplicativo.
     *
     */
    fun getPendingIntentForNotificationSettings(): PendingIntent {
        val intent = when {

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                // API 26+: vai direto pro canal
                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, baseContext.packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                }
            }

            else -> {
                // API 24 e 25: abre tela de configurações gerais do app
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
            }

        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        return PendingIntent.getActivity(
            baseContext, 220462, intent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 220461
    }
}
