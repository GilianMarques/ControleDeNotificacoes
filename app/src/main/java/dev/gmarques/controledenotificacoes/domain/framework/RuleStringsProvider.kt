package dev.gmarques.controledenotificacoes.domain.framework

/**
 *
 * Fornece recursos de strings relacionados a regras de negócio e dias da semana.
 *
 * Esta interface garante que a camada de domínio permaneça independente da plataforma ou framework específico,
 * aderindo ao princípio de separação de responsabilidades da Clean Architecture. Ela permite que a camada de
 * domínio acesse representações em string de regras e dias sem depender diretamente de recursos de string
 * específicos da plataforma como o Context.
 *
 * Criado por Gilian Marques
 * Em Quarta-feira, 16 de abril de 2025, às 20:51.
 */
interface RuleStringsProvider {

    fun monday(): String
    fun tuesday(): String
    fun wednesday(): String
    fun thursday(): String
    fun friday(): String
    fun saturday(): String
    fun sunday(): String

    fun permissive(): String
    fun restrictive(): String
    fun wholeDay(): String
    fun everyDay(): String

}