@file:Suppress("unused")

package dev.gmarques.controledenotificacoes.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.framework.RuleStringsProvider
import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.domain.framework.VibratorInterface
import dev.gmarques.controledenotificacoes.domain.framework.notification_service.RuleEnforcer
import dev.gmarques.controledenotificacoes.framework.RuleStringsProviderImpl
import dev.gmarques.controledenotificacoes.framework.ScheduleManagerImpl
import dev.gmarques.controledenotificacoes.framework.VibratorImpl
import dev.gmarques.controledenotificacoes.framework.notification_service.RuleEnforcerImpl

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 02 de abril de 2025 as 22:24.
 *
 * Modulo voltado às dependencias relacionadas a plataforma.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformModule {

    @Binds
    abstract fun bindVibrator(vibrator: VibratorImpl): VibratorInterface

    @Binds
    abstract fun bindRuleStringsProvider(strProvider: RuleStringsProviderImpl): RuleStringsProvider

    @Binds
    abstract fun bindRuleEnforcer(strProvider: RuleEnforcerImpl): RuleEnforcer

    @Binds
    abstract fun bindScheduleManager(strProvider: ScheduleManagerImpl): ScheduleManager

}