package dev.gmarques.controledenotificacoes.domain.usecase.user

import dev.gmarques.controledenotificacoes.domain.data.repository.UserRepository
import dev.gmarques.controledenotificacoes.domain.model.User
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 11:51.
 */
class GetUserUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke(): User? {
        return repository.getUser()
    }
}