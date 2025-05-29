package dev.gmarques.controledenotificacoes.domain.data.repository

import dev.gmarques.controledenotificacoes.domain.model.User

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 10:53.
 */
interface UserRepository {
    fun getUser(): User?
    suspend fun logOff()
    fun deleteAccount()
}