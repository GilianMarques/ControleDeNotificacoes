package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.framework.notification_service.RuleEnforcer
import dev.gmarques.controledenotificacoes.framework.ReportNotificationManager
import dev.gmarques.controledenotificacoes.framework.ScheduleManagerImpl

/**
 * Criado por Gilian Marques
 * Em sábado, 24 de maio de 2025 as 15:58.
 */
@InstallIn(SingletonComponent::class)
@EntryPoint
interface FrameworkEntryPoint {
    fun reportNotificationManager(): ReportNotificationManager
    fun ruleEnforcer(): RuleEnforcer
    fun scheduleManager(): ScheduleManagerImpl

}