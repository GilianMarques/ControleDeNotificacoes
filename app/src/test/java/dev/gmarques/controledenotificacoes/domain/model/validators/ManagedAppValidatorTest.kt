package dev.gmarques.controledenotificacoes.domain.model.validators

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.ManagedAppValidator
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ManagedAppValidatorTest {

    @Test
    fun `ao validar packageId valido deve retornar sucesso`() {
        val result = ManagedAppValidator.validatePackageId("com.exemplo.app")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `ao validar packageId vazio deve retornar falha com BlankStringException`() {
        val result = ManagedAppValidator.validatePackageId("")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ManagedAppValidator.ManagedAppValidatorException.BlankPackageIdException)
    }

    @Test
    fun `ao validar ruleId valido deve retornar sucesso`() {
        val result = ManagedAppValidator.validateRuleId("regra123")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `ao validar ruleId vazio deve retornar falha com BlankStringException`() {
        val result = ManagedAppValidator.validateRuleId("")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ManagedAppValidator.ManagedAppValidatorException.BlankRuleIdException)
    }

    @Test
    fun `ao validar managedApp com dados validos deve passar sem excecao`() {
        val managedApp = ManagedApp(packageId = "com.exemplo.app", ruleId = "regra123", hasPendingNotifications = false)
        try {
            ManagedAppValidator.validate(managedApp)
        } catch (e: Exception) {
            fail("Não era esperada nenhuma exceção, mas foi lançada: ${e.message}")
        }
    }

    @Test
    fun `ao validar managedApp com packageId invalido deve lancar BlankStringException`() {
        val managedApp = ManagedApp(packageId = "", ruleId = "regra123", hasPendingNotifications = false)
        try {
            ManagedAppValidator.validate(managedApp)
            fail("Era esperada uma BlankStringException")
        } catch (e: Exception) {
            assertTrue(e is ManagedAppValidator.ManagedAppValidatorException.BlankPackageIdException)
        }
    }

    @Test
    fun `ao validar managedApp com ruleId invalido deve lancar BlankStringException`() {
        val managedApp = ManagedApp(packageId = "com.exemplo.app", ruleId = "", hasPendingNotifications = false)
        try {
            ManagedAppValidator.validate(managedApp)
            fail("Era esperada uma BlankStringException")
        } catch (e: Exception) {
            assertTrue(e is ManagedAppValidator.ManagedAppValidatorException.BlankRuleIdException)
        }
    }
}
