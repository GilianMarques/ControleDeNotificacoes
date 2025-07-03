package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dev.gmarques.controledenotificacoes.BuildConfig
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
    private val echoImpl = HiltEntryPoints.echo()
    private var cancelingNotificationKey = ""
    private var errorJob: Job? = null
    private var validationCallbackErrorJob: Job? = null

    companion object {
        var instance: NotificationListener? = null
            private set
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT //https://blog.stackademic.com/exploring-the-notification-listener-service-in-android-7db54d65eca7
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this@NotificationListener
        observeRulesChanges()
    }

    /**
     * Observa mudanças nas regras de notificação.
     * Quando uma mudança é detectada (uma regra é adicionada, removida ou atualizada),
     * o mét.odo [reEvaluateActiveNotifications] é chamado para reavaliar todas as notificações ativas
     * com base nas regras atualizadas. Isso garante que as regras sejam aplicadas dinamicamente.
     */
    private fun observeRulesChanges() = launch(IO) {
        HiltEntryPoints.observeAllRulesUseCase().invoke().collect { rules ->
            reEvaluateActiveNotifications()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        manageNotification(sbn)
    }

    /**
     * Lê todas as notificações ativas no momento em que o serviço é conectado.
     * Processa cada notificação ativa usando o mét.odo [manageNotification].
     */

    fun reEvaluateActiveNotifications() {
        val active = activeNotifications ?: return
        active.forEach { sbn ->
            manageNotification(sbn)
        }

    }

    fun getFilteredActiveNotifications(): List<StatusBarNotification> {

        val notifications = mutableListOf<StatusBarNotification>()
        activeNotifications?.let {
            notifications.addAll(it)
        }

        return notifications.filter {
            !it.isOngoing && it.packageName != BuildConfig.APPLICATION_ID
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
                    Log.d("USUK", "NotificationListener.cancelNotification: ${sbn.packageName} ")
                    cancelValidationCallbackTimer()
                    crashIfNotificationWasNotRemovedInDebugBuild(sbn)
                    cancelNotification(sbn.key)
                }

                override fun appNotManaged() {
                    Log.d("USUK", "NotificationListener.appNotManaged: ${sbn.packageName}")
                    cancelValidationCallbackTimer()
                    echoImpl.repostIfNotification(sbn)
                }

                override fun allowNotification() {
                    Log.d("USUK", "NotificationListener.allowNotification: ${sbn.packageName}")
                    cancelValidationCallbackTimer()
                    echoImpl.repostIfNotification(sbn)
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
    private fun cancelValidationCallbackTimer() = validationCallbackErrorJob?.cancel()

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
            delay(3000)
            error("O callback de validação passado para o RuleEnforcer não foi chamado.")
        }
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
        instance = null
        cancel()
        super.onListenerDisconnected()
    }

}

