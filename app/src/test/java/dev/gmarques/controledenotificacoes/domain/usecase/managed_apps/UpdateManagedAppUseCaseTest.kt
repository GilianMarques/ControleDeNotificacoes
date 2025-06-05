package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateManagedAppUseCaseTest {

    private lateinit var repository: ManagedAppRepository
    private lateinit var useCase: UpdateManagedAppUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = UpdateManagedAppUseCase(repository)
    }

    @Test
    fun `deve chamar update do repositorio com o managed app correto`() = runTest {

        val managedApp = ManagedApp(packageId = "com.whatsapp", ruleId = "regra-123", hasPendingNotifications = false)
        useCase(managedApp)
        coVerify(exactly = 1) { repository.updateManagedAppOrThrow(managedApp) }
    }
}
