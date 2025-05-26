package dev.gmarques.controledenotificacoes.domain.model

import androidx.annotation.Keep
import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 10:53.
 *
 * Embrulha os dados de usuário obtidos através do Fireabse Auth
 *
 * Esse objeto foi criado com o objetivo de facilitar os testes do aplicativo
 * permitindo rodar o app sem que seja necessário autenticar o usuário.
 *
 */
@Keep
data class User(
    val name: String,
    val email: String,
    val photoUrl: String,
) : Serializable