package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.usecase.GetUserUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun getAppUserUseCase(): GetUserUseCase
}