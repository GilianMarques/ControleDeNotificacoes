package dev.gmarques.controledenotificacoes.framework


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.framework.ActiveNotificationRepository
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:34.
 */
class ActiveNotificationRepositoryImpl @Inject constructor(@ApplicationContext private val appContext: Context) :
    ActiveNotificationRepository {
    var receiver: BroadcastReceiver? = null

    override fun getActiveNotifications(callback: ActiveNotificationRepository.Callback) {
        Log.d("USUK", "ActiveNotificationRepositoryImpl.getActiveNotifications: ")

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                val list = intent?.getParcelableArrayListExtra<StatusBarNotification>(EXTRA_NOTIFICATIONS).orEmpty()
                Log.d("USUK", "ActiveNotificationRepositoryImpl.onReceive: $list")
                callback.onActiveNotificationsReceived(list)
                appContext.unregisterReceiver(receiver)
            }
        }

        registerThisReceiver(receiver!!)

        NotificationListener.sendBroadcastToReadActiveNotifications()
    }

    private fun registerThisReceiver(receiver: BroadcastReceiver) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, IntentFilter(INTENT_FILTER_FOR_BROADCAST), RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION") @SuppressLint("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(receiver, IntentFilter(INTENT_FILTER_FOR_BROADCAST))
        }
    }

    companion object {
        const val INTENT_FILTER_FOR_BROADCAST = "NotificationListener.ActiveNotificationsResponse"
        const val EXTRA_NOTIFICATIONS = "extra_active_notifications"
    }
}

