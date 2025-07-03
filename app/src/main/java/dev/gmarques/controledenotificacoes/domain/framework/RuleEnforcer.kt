package dev.gmarques.controledenotificacoes.domain.framework

import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 14:10.
 *
 * Abstração na camada de domínio que serve como contrato para a implementação de uma classe que vai verificar a existência de
 * regras para um determinado app, e avaliar, se a notificação emitida por esse app
 * deve ou não ser bloqueada assim como manter histórico das notificações de apps gerenciados, Através dos usecases.
 */
interface RuleEnforcer {
    suspend fun enforceOnNotification(
        sbn: StatusBarNotification,
        callback: Callback,
    )

    interface Callback {
        fun cancelNotification(appNotification: AppNotification, rule: Rule, managedApp: ManagedApp)
        fun appNotManaged()
        fun allowNotification()
    }

}