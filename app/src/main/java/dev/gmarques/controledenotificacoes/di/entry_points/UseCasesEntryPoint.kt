package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmsOnBootUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.CheckAppInBlockPeriodUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.NextAppUnlockTimeUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleDescriptionUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UseCasesEntryPoint {
    fun getAppUserUseCase(): GetUserUseCase
    fun rescheduleAlarmsOnBootUseCase(): RescheduleAlarmsOnBootUseCase
    fun nextAppUnlockUseCase(): NextAppUnlockTimeUseCase
    fun checkAppInBlockPeriodUseCase(): CheckAppInBlockPeriodUseCase
    fun generateRuleNameUseCase(): GenerateRuleDescriptionUseCase
}