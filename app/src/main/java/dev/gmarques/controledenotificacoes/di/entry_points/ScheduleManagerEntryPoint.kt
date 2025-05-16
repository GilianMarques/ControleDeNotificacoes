package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.framework.ScheduleManagerImpl

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 16 de maio de 2025 as 11:48.
 */
@InstallIn(SingletonComponent::class)
@EntryPoint
interface ScheduleManagerEntryPoint {
    fun getScheduleManager(): ScheduleManagerImpl
}