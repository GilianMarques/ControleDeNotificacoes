package dev.gmarques.controledenotificacoes.domain.utils

import android.content.Context
import dev.gmarques.controledenotificacoes.R

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 31 de março de 2025 as 23:48.
 */
object IntExtFuns {

    /**
     * Converte um valor em Density-independent Pixels (DP) para pixels (PX).
     *
     * Esta função de extensão para a classe `Int` permite converter um valor inteiro representando uma medida em DP
     * para o seu equivalente em pixels, considerando a densidade da tela do dispositivo.
     *
     * @param context O contexto da aplicação, necessário para obter as informações de densidade da tela.
     * @return O valor em pixels correspondente ao valor em DP fornecido.
     * @throws IllegalArgumentException Se o contexto fornecido for inválido.
     */
    fun Int.toDp(context: Context): Int {
        val dimension = context.resources.getDimension(R.dimen.dp)
        return (dimension * this).toInt()
    }
}