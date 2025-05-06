package dev.gmarques.controledenotificacoes.domain.repository

/**
 * Criado por Gilian Marques
 * Em terça-feira, 06 de maio de 2025 as 13:22.
 */
interface PreferencesRepository {

    suspend fun <T> save(key: String, value: T)

    suspend fun <T> read(key: String, default: T): T

    suspend fun clearAll()
}