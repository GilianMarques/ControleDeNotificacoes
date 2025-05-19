package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmsOnBootUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UseCasesEntryPoint {
    fun getAppUserUseCase(): GetUserUseCase
    fun rescheduleAlarmsOnBootUseCase(): RescheduleAlarmsOnBootUseCase
}