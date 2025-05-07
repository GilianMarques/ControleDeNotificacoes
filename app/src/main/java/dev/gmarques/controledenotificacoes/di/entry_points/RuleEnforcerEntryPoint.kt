package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.framework.notification_service.RuleEnforcer

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RuleEnforcerEntryPoint {
    fun getRuleEnforcer(): RuleEnforcer
}