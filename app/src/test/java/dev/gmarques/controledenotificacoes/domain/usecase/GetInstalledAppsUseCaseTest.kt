package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.repository.AppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GetInstalledAppsUseCaseTest {

    private lateinit var useCase: GetInstalledAppsUseCase
    private lateinit var repository: AppRepository

    @BeforeEach
    fun configurar() {
        repository = mock()
        useCase = GetInstalledAppsUseCase(repository)
    }

    @Test
    fun `dado um nome alvo e pacotes excluidos, quando executar e chamado, entao o repositorio getInstalledApps deve ser invocado`() =
        runTest {
            val excludedPackages = hashSetOf("com.facebook.katana")
            val targetName = "chat"

            val expectedApps = listOf(
                mockApp("WhatsApp", "com.whatsapp"),
                mockApp("Telegram", "org.telegram.messenger")
            )

            whenever(repository.getInstalledApps(targetName, excludedPackages)).thenReturn(expectedApps)

            val result = useCase(targetName, excludedPackages)

            assertEquals(expectedApps, result)
            verify(repository).getInstalledApps(targetName, excludedPackages)
    }

    @Test
    fun `dado um nome vazio e sem pacotes excluidos, quando executar e chamado, entao o repositorio getInstalledApps deve retornar todos os apps nao excluidos`() =
        runTest {
            val excludedPackages = hashSetOf<String>()
            val targetName = ""

            val expectedApps = listOf(
                mockApp("Instagram", "com.instagram.android"),
                mockApp("Twitter", "com.twitter.android")
            )

            whenever(repository.getInstalledApps(targetName, excludedPackages)).thenReturn(expectedApps)

            val result = useCase(targetName, excludedPackages)

            assertEquals(expectedApps, result)
            verify(repository).getInstalledApps(targetName, excludedPackages)
    }

    private fun mockApp(name: String, packageId: String): InstalledApp {
        return InstalledApp(name, packageId, mock())
    }
}
