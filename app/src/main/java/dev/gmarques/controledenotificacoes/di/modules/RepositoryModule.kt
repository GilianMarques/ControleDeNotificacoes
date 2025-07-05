@file:Suppress("unused")

package dev.gmarques.controledenotificacoes.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.data.repository.AppNotificationRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.InstalledInstalledAppRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.ManagedAppRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.PreferencesRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.RuleRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.UserRepositoryImpl
import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.InstalledAppRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.PreferencesRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.UserRepository
import javax.inject.Singleton


/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@Module
@InstallIn(SingletonComponent::class) // mesma instancia disponivel em toda aplicação
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRuleRepository(impl: RuleRepositoryImpl): RuleRepository

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: InstalledInstalledAppRepositoryImpl): InstalledAppRepository

    @Binds
    @Singleton
    abstract fun bindManagedAppRepository(impl: ManagedAppRepositoryImpl): ManagedAppRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAppNotificationRepository(impl: AppNotificationRepositoryImpl): AppNotificationRepository
}