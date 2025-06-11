package dev.gmarques.controledenotificacoes.domain.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PreferencePropertyTest {

    private lateinit var storedValues: MutableMap<String, Any>
    private lateinit var preference: PreferenceProperty<String>

    @BeforeEach
    fun setup() {
        storedValues = mutableMapOf()

        preference = PreferenceProperty(
            key = "testKey",
            defaultValue = "default",
            preferenceReader = { key, default -> storedValues[key] as? String ?: default },
            preferenceSaver = { key, value -> storedValues[key] = value }
        )
    }

    @Test
    fun `ao acessar value deve retornar o valor salvo ou o padrao`() {
        assertEquals("default", preference.value)

        preference("novoValor")

        assertEquals("novoValor", preference.value)
    }

    @Test
    fun `ao chamar reset deve restaurar o valor padrao`() {
        preference("outroValor")
        assertEquals("outroValor", preference.value)

        preference.reset()
        assertEquals("default", preference.value)
    }

    @Test
    fun `isDefault deve retornar true se o valor for padrao`() {
        assertTrue(preference.isDefault())

        preference("modificado")
        assertFalse(preference.isDefault())

        preference.reset()
        assertTrue(preference.isDefault())
    }
}
