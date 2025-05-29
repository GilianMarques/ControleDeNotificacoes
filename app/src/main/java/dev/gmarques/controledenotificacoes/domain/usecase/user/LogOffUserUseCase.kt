package dev.gmarques.controledenotificacoes.domain.usecase.user

import dev.gmarques.controledenotificacoes.domain.data.repository.UserRepository
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 11:59.
 */
class LogOffUserUseCase @Inject constructor(private val repository: UserRepository) {

    suspend operator fun invoke() {
        repository.logOff()
    }
}