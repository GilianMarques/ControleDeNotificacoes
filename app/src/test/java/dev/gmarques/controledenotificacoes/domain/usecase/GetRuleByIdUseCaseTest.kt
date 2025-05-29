package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class GetRuleByIdUseCaseTest {

    private lateinit var useCase: GetRuleByIdUseCase
    private val repository: RuleRepository = mock()

    @BeforeEach
    fun setUp() {
        useCase = GetRuleByIdUseCase(repository)
    }

    @Test
    fun `dado um id, quando execute for chamado, entao repositorio getRuleById deve ser invocado`() = runTest {
        val id = "radlkhjglç"

        useCase(id)

        verify(repository).getRuleById(id)
    }
}
