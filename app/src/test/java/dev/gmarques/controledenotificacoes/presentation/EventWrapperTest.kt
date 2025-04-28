package dev.gmarques.controledenotificacoes.presentation

import dev.gmarques.controledenotificacoes.presentation.EventWrapper
import org.junit.Assert.*
import org.junit.Test

class EventWrapperTest {

    @Test
    fun `deve retornar o evento na primeira chamada`() {
        val wrapper = EventWrapper("evento")

        val resultado = wrapper.consume()

        assertEquals("evento", resultado)
    }

    @Test
    fun `nao deve retornar o evento se ja foi consumido`() {
        val wrapper = EventWrapper("evento")

        wrapper.consume() // primeira chamada
        val resultado = wrapper.consume() // segunda chamada

        assertNull(resultado)
    }

    @Test
    fun `deve retornar null se o evento for null`() {
        val wrapper = EventWrapper<String?>(null)

        val resultado = wrapper.consume()

        assertNull(resultado)
    }
}
