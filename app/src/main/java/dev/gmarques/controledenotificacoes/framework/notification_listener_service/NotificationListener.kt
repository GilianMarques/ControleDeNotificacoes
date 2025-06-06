package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Criado por Gilian Marques
 * Em sábado, 03 de maio de 2025 as 16:18.
 */
class NotificationListener : NotificationListenerService(), CoroutineScope by MainScope() {

    private val ruleEnforcer = HiltEntryPoints.ruleEnforcer()
    private var cancelingNotificationKey = ""
    private var errorJob: Job? = null

    companion object {
        private const val ACTION_KEY = "ACTION"
        private const val ACTION_READ_ACTIVE_NOTIFICATIONS = "READ_ACTIVE_NOTIFICATIONS"
        private const val INTENT_FILTER_FOR_BROADCAST = "NotificationListener.BroadcastReceiver"

        /**
         * Envia um broadcast para a instancia desse seviço em execução para re-executar a validação das notificações ativas.
         */
        fun sendBroadcastToReadActiveNotifications() {
            val intent = Intent(INTENT_FILTER_FOR_BROADCAST).apply {
                setPackage(App.context.packageName)
                putExtra(ACTION_KEY, ACTION_READ_ACTIVE_NOTIFICATIONS)
            }
            App.context.sendBroadcast(intent)
        }


    }

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra(ACTION_KEY)) {
                ACTION_READ_ACTIVE_NOTIFICATIONS -> readActiveNotifications()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(commandReceiver, IntentFilter(INTENT_FILTER_FOR_BROADCAST), RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            registerReceiver(commandReceiver, IntentFilter(INTENT_FILTER_FOR_BROADCAST))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT //https://blog.stackademic.com/exploring-the-notification-listener-service-in-android-7db54d65eca7
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("USUK", "NotificationListener.onListenerConnected: ")
        readActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        manageNotification(sbn)
    }

    /**
     * Lê todas as notificações ativas no momento em que o serviço é conectado.
     * Processa cada notificação ativa usando o mét.odo [manageNotification].
     */
    private fun readActiveNotifications() {
        Log.d("USUK", "NotificationListener.readActiveNotifications: ")
        val active = activeNotifications ?: return
        active.forEach { sbn ->
            manageNotification(sbn)
        }
    }

    /**
     * Processa uma notificação recebida.
     * Extrai informações relevantes e as encapsula em um objeto AppNotification.
     * Em seguida, utiliza o RuleEnforcer para aplicar as regras configuradas.
     *
     */
    private fun manageNotification(sbn: StatusBarNotification) {

        if (sbn.isOngoing) {
            //  Log.d("USUK", "NotificationListener.manageNotification: can't dismiss on going notifications from ${sbn.packageName}")
            return
        }

        runBlocking {
            ruleEnforcer.enforceOnNotification(sbn) { not, rule, managedApp ->
                Log.d(
                    "USUK",
                    "NotificationListener.manageNotification: cancelling: ${not.title} - ${not.packageId} isOngoing ${sbn.isOngoing}"
                )

                crashIfNotificationWasntRemovedInDebugBuild(sbn)
                cancelNotification(sbn.key)
            }
        }
    }

    /**
     * Essa função serve pra testes apenas e nao sera usada em produção.
     * Caso alguma alteraçao que impeça o bloqueio das notificações seja feita (como ja foi feita antes...)
     * essa função vai crashar o app para que o jumento do desenvolvedor (eu :-] ) possa ajeitar a cagada que ele fez
     */
    private fun crashIfNotificationWasntRemovedInDebugBuild(sbn: StatusBarNotification) {
        if (BuildConfig.DEBUG) {
            if (sbn.isOngoing) return // nao da pra cancelar notificaçoes ongoing

            cancelingNotificationKey = sbn.key
            errorJob?.cancel()
            errorJob = CoroutineScope(Main).launch {
                delay(1000)
                error("A notificaçao nao foi cancelada: OnGoing?${sbn.isOngoing}\nMais detalhes:$sbn")
            }
        }
    }

    /**
     * Ajuda  a  [crashIfNotificationWasntRemovedInDebugBuild] a determinar se a notificação foi de fato cancelada
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?) {

        if (BuildConfig.DEBUG) {
            if (sbn?.key == cancelingNotificationKey) errorJob?.cancel()
        }

        super.onNotificationRemoved(sbn, rankingMap)
    }

    override fun onListenerDisconnected() {
        cancel()
        super.onListenerDisconnected()
        Log.d("USUK", "NotificationListener.".plus("onListenerDisconnected() "))
    }

}

