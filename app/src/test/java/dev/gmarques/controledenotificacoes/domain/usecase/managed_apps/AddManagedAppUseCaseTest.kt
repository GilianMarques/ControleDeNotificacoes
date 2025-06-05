package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddManagedAppUseCaseTest {

    private lateinit var repository: ManagedAppRepository
    private lateinit var useCase: AddManagedAppUseCase

    @Before
    fun configurar() {
        repository = mockk(relaxed = true)
        useCase = AddManagedAppUseCase(repository)
    }

    @Test
    fun `deve chamar add do repositorio com o managed app correto`() = runTest {
        // Arrange
        val managedApp = ManagedApp(packageId = "com.instagram.android", ruleId = "regra-456", hasPendingNotifications = false)

        // Act
        useCase(managedApp)

        // Assert
        coVerify(exactly = 1) { repository.addOrUpdateManagedAppOrThrow(managedApp) }
    }
}
