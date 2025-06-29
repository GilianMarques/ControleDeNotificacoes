package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.framework.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
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
    private var validationCallbackErrorJob: Job? = null

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
            @Suppress("DEPRECATION") @SuppressLint("UnspecifiedRegisterReceiverFlag") registerReceiver(
                commandReceiver, IntentFilter(INTENT_FILTER_FOR_BROADCAST)
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT //https://blog.stackademic.com/exploring-the-notification-listener-service-in-android-7db54d65eca7
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("USUK", "NotificationListener.onListenerConnected: ")
        observeRulesChanges()
    }

    /**
     * Observa mudanças nas regras de notificação.
     * Quando uma mudança é detectada (uma regra é adicionada, removida ou atualizada),
     * o mét.odo [readActiveNotifications] é chamado para reavaliar todas as notificações ativas
     * com base nas regras atualizadas. Isso garante que as regras sejam aplicadas dinamicamente.
     */
    private fun observeRulesChanges() = launch(IO) {
        HiltEntryPoints.observeAllRulesUseCase().invoke().collect { rules ->
            readActiveNotifications()
        }
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

        if (sbn.isOngoing) return
        if (sbn.packageName.contains(BuildConfig.APPLICATION_ID)) return

        runBlocking {
            crashIfCallbackNotCalled()
            ruleEnforcer.enforceOnNotification(sbn, object : RuleEnforcer.Callback {

                override fun cancelNotification(
                    appNotification: AppNotification,
                    rule: Rule,
                    managedApp: ManagedApp,
                ) {
                    Log.d("USUK", "NotificationListener.cancelNotification: ")
                    cancelValidationCallbackTimer()
                    crashIfNotificationWasNotRemovedInDebugBuild(sbn)
                    cancelNotification(sbn.key)
                }

                override fun appNotManaged() {
                    Log.d("USUK", "NotificationListener.appNotManaged: ")
                    cancelValidationCallbackTimer()
                    validateAndEchoNotification(sbn)
                }

                override fun allowNotification() {
                    Log.d("USUK", "NotificationListener.allowNotification: ")
                    cancelValidationCallbackTimer()
                    validateAndEchoNotification(sbn)
                }
            })
        }
    }

    /**
     * Cancela o temporizador que monitora se o callback de validação da notificação foi chamado.
     * Esta função é usada em conjunto com [crashIfCallbackNotCalled] para garantir que,
     * em builds de debug, o aplicativo falhe se o callback não for invocado dentro de um
     * período esperado. Isso ajuda a identificar problemas onde o RuleEnforcer não está
     * chamando o callback corretamente, o que poderia afetar a função de eco.
     */
    private fun cancelValidationCallbackTimer() {
        validationCallbackErrorJob?.cancel()
    }

    /**
     * Inicia um temporizador que, se não for cancelado a tempo, causará uma falha no aplicativo.
     * Esta função é destinada a evitar bugs. Ela garante que alterações no RuleEnforcer
     * não impeçam que o callback seja chamado nos casos onde:
     * - A notificação deve ser bloqueada.
     * - A notificação não deve ser bloqueada.
     * - O aplicativo não é gerenciado.
     * Isso serve para impedir que bugs sejam introduzidos no código.
     *
     * @see  cancelValidationCallbackTimer
     * @see manageNotification
     */
    private fun crashIfCallbackNotCalled() {
        validationCallbackErrorJob = CoroutineScope(Main).launch {
            delay(1000)
            error("O callback de validação passado para o RuleEnforcer não foi chamado.")
        }
    }

    private fun validateAndEchoNotification(sbn: StatusBarNotification) {
        if (PreferencesImpl.echoEnabled.isDefault()) return
        if (isMediaPlaybackNotification(sbn)) return

        repostNotificationIfEnabled(sbn)
    }

    private fun repostNotificationIfEnabled(sbn: StatusBarNotification) {
        val original = sbn.notification
        val notificationId = sbn.id + 10000
        val notificationTag = sbn.tag

        val notificationManager = baseContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val echoChannel = "echo_channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                echoChannel, getString(R.string.Canal_echo), NotificationManager.IMPORTANCE_DEFAULT
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

        Log.d(
            "USUK",
            "NotificationListener.echoNotification: reposting ${original.extras.getCharSequence(Notification.EXTRA_TITLE)}"
        )

        Handler(Looper.getMainLooper()).postDelayed({
            notificationManager.cancel(notificationTag, notificationId)
        }, 1000)
    }

    /**Não reposta notificações de apps de musica ou video*/
    private fun isMediaPlaybackNotification(sbn: StatusBarNotification): Boolean {
        // Verifica se há estilo de media (MediaStyle)
        return sbn.notification.extras.getString(Notification.EXTRA_TEMPLATE)?.contains("MediaStyle") == true
    }

    /**
     * Essa função serve pra testes apenas e nao sera usada em produção.
     * Caso alguma alteraçao que impeça o bloqueio das notificações seja feita (como ja foi feita antes...)
     * essa função vai crashar o app para que o jumento do desenvolvedor (eu :-] ) possa ajeitar a cagada que ele fez
     */
    private fun crashIfNotificationWasNotRemovedInDebugBuild(sbn: StatusBarNotification) {
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
     * Ajuda  a  [crashIfNotificationWasNotRemovedInDebugBuild] a determinar se a notificação foi de fato cancelada
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

