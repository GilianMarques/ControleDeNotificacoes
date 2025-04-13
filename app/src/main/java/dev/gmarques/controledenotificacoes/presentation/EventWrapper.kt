package dev.gmarques.controledenotificacoes.presentation

/**
 * Criado por Gilian Marques
 * Em sábado, 12 de abril de 2025 as 16:58.
 * Essa classe auxilia no envio de eventos para a UI
 */
class EventWrapper<T>(val event: T?) {

    private var consumed = false

    /**
     * Consome o evento, marcando-o como processado.
     *
     * Esta função recupera o evento se ele ainda não tiver sido consumido e o marca como consumido.
     * Chamadas subsequentes a esta função retornarão `null`.
     *
     * @return O evento se ele não foi consumido anteriormente, `null` caso contrário.
     */
    fun consume(): T? {
        if (consumed) return null
        consumed = true
        return event
    }

}