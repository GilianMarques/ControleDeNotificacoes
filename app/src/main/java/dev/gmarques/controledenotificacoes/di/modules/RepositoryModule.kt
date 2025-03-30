package dev.gmarques.controledenotificacoes.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.data.repository.RuleRepositoryImpl
import dev.gmarques.controledenotificacoes.domain.repository.RuleRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Repositório deve ter escopo Singleton
/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRuleRepository(impl: RuleRepositoryImpl): RuleRepository
}