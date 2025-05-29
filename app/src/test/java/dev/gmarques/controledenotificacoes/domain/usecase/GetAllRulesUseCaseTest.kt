package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetAllRulesUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class GetAllRulesUseCaseTest {

    private lateinit var useCase: GetAllRulesUseCase
    private val repository: RuleRepository = mock()

    @BeforeEach
    fun setUp() {
        useCase = GetAllRulesUseCase(repository)
    }

    @Test
    fun `quando execute for chamado, então repositorio getAllRules deve ser invocado`() = runTest {
        useCase()

        verify(repository).getAllRules()
    }
}
