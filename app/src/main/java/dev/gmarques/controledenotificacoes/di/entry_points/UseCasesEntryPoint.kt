package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmsOnBootUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.ScheduleAutoTurnOnUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.IsAppInBlockPeriodUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.NextAppUnlockTimeUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleDescriptionUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveAllRulesUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UseCasesEntryPoint {
    fun getAppUserUseCase(): GetUserUseCase
    fun rescheduleAlarmsOnBootUseCase(): RescheduleAlarmsOnBootUseCase
    fun nextAppUnlockUseCase(): NextAppUnlockTimeUseCase
    fun isAppInBlockPeriodUseCase(): IsAppInBlockPeriodUseCase
    fun generateRuleNameUseCase(): GenerateRuleDescriptionUseCase
    fun updateManagedAppUseCase(): UpdateManagedAppUseCase
    fun insertAppNotificationUseCase(): InsertAppNotificationUseCase
    fun observeAllRulesUseCase(): ObserveAllRulesUseCase
    fun scheduleAutoTurnOnUseCase(): ScheduleAutoTurnOnUseCase
}