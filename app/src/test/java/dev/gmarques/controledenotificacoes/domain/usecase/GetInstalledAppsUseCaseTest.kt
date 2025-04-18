package dev.gmarques.controledenotificacoes.domain.usecase

import android.graphics.drawable.Drawable
import dev.gmarques.controledenotificacoes.domain.repository.AppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class GetInstalledAppsUseCaseTest {

    private lateinit var repository: AppRepository
    private lateinit var useCase: GetInstalledAppsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetInstalledAppsUseCase(repository)
    }

    @Test
    fun `retorna lista vazia quando targetName e excludePackages sao vazios`() = runTest {
        coEvery { repository.getInstalledApps("", hashSetOf()) } returns emptyList()

        val result = useCase("", hashSetOf())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `retorna apps que correspondem ao targetName quando excludePackages esta vazio`() = runTest {
        val apps = listOf(
            mockApp("com.app.game", "GameApp", false),
            mockApp("com.app.galeria", "Galeria de Fotos", false)
        )
        coEvery { repository.getInstalledApps("galeria", hashSetOf()) } returns listOf(apps[1])

        val result = useCase("galeria", hashSetOf())

        assertEquals(1, result.size)
        assertEquals("Galeria de Fotos", result[0].name)
    }

    @Test
    fun `retorna apps que correspondem ao targetName e marca os que estao em excludePackages`() = runTest {
        val preSelected = hashSetOf("com.app.jogo")
        val apps = listOf(
            mockApp("com.app.jogo", "Meu Jogo", true),
            mockApp("com.app.jogovelho", "Jogo Velho", false)
        )
        coEvery { repository.getInstalledApps("jogo", preSelected) } returns apps

        val result = useCase("jogo", preSelected)

        assertEquals(2, result.size)
        assertEquals(true, result.first().preSelected)
    }

    @Test
    fun `retorna lista vazia quando nenhum app corresponde ao targetName`() = runTest {
        coEvery { repository.getInstalledApps("inexistente", any()) } returns emptyList()

        val result = useCase("inexistente", hashSetOf())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `ignora pacotes invalidos em excludePackages`() = runTest {

        val preSelected = hashSetOf("com.invalido.app", "com.app.valido")

        val apps = listOf(mockApp("com.app.valido", "App Valido", true))

        coEvery { repository.getInstalledApps("", preSelected) } returns apps

        val result = useCase("", preSelected)

        assertEquals(1, result.size)
        assertEquals("com.app.valido", result[0].packageId)
    }

    @Test
    fun `lida com targetName contendo caracteres especiais`() = runTest {
        val apps = listOf(mockApp("com.app.exemplo", "Música!", false))
        coEvery { repository.getInstalledApps("Música!", any()) } returns apps

        val result = useCase("Música!", hashSetOf())

        assertEquals(1, result.size)
        assertEquals("Música!", result[0].name)
    }

    @Test
    fun `trunca ou ignora targetName muito longo`() = runTest {
        val longTarget = "a".repeat(10_000)
        coEvery { repository.getInstalledApps(longTarget, any()) } returns emptyList()

        val result = useCase(longTarget, hashSetOf())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `processa grande volume de excludePackages corretamente`() = runTest {

        val preSelected = (1..1000).map { "com.app.$it" }.toHashSet()
        val apps = listOf(mockApp("com.app.500", "App 500", true))

        coEvery { repository.getInstalledApps("", preSelected) } returns apps

        val result = useCase("", preSelected)

        assertEquals(1, result.size)
        assertTrue(result[0].preSelected)
    }


    // Funcao auxiliar para criar um mock de InstalledApp
    private fun mockApp(packageId: String, name: String, preSelected: Boolean): InstalledApp {
        val icon = mockk<Drawable>()
        return InstalledApp(name, packageId, icon, preSelected)
    }
}
