package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.framework.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.ReadPreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.SavePreferenceUseCase
import dev.gmarques.controledenotificacoes.framework.AlarmSchedulerImpl
import dev.gmarques.controledenotificacoes.framework.EchoImpl
import dev.gmarques.controledenotificacoes.framework.report_notification.ReportNotificationManager

/**
 * Criado por Gilian Marques
 * Em sábado, 24 de maio de 2025 as 15:58.
 */
@InstallIn(SingletonComponent::class)
@EntryPoint
interface FrameworkEntryPoint {
    fun reportNotificationManager(): ReportNotificationManager
    fun ruleEnforcer(): RuleEnforcer
    fun scheduleManager(): AlarmSchedulerImpl
    fun readPreferenceUseCase(): ReadPreferenceUseCase
    fun savePreferenceUseCase(): SavePreferenceUseCase
    fun echo(): EchoImpl
}