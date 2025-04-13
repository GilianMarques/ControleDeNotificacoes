package dev.gmarques.controledenotificacoes.domain.exceptions

/**
 * Criado por Gilian Marques
 * Em domingo, 30 de março de 2025 as 14:21.
 *
 * Util para ajudar a validar comprimentos minimos e maximos permitidos de strings (quantidade de caracteres)
 * e listas  (quantidade de objetos na lista) e ranges no geral
 *
 * A mensgem predefinida evita repetição de codigo e as propriedades permitem a confeção de mensagens
 * personalizadas na camada de UI
 */
class OutOfRangeException(
    val minLength: Int,
    val maxLength: Int,
    val actual: Int,
) : Exception("O range valido é de $minLength a $maxLength. valor atual: $actual")