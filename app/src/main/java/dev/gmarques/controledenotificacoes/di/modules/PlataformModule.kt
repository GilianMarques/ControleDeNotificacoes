package dev.gmarques.controledenotificacoes.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.plataform.VibratorInterface
import dev.gmarques.controledenotificacoes.plataform.VibratorImpl

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 02 de abril de 2025 as 22:24.
 *
 * Modulo voltado às dependencias relacionadas a plataforma.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlataformModule {
    // TODO: ver anotaçao pro R8 nao ofuscar classes do hilt se necessario
    @Binds
    abstract fun bindVibrator(vibrator: VibratorImpl): VibratorInterface
}