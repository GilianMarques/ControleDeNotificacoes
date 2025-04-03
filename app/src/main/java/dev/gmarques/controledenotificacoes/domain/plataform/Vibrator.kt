package dev.gmarques.controledenotificacoes.domain.plataform

interface Vibrator {
    fun error()
    fun success()
    fun interaction()

}
