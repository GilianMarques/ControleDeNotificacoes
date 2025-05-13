package dev.gmarques.controledenotificacoes.framework

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.framework.RuleStringsProvider
import javax.inject.Inject

/**
 * Tem como proposito garantir que a camada de dominio nao dependa diretamente da plataforma
 * garantindo o principio de separação de camadas da clean architecture.
 *
 * Implementação concreta de [RuleStringsProvider] que fornece strings localizadas
 * para termos relacionados a regras.
 *
 * Esta classe utiliza o contexto da aplicação para acessar os recursos de string de
 * `R.string`. Ela fornece representações em string para os dias da semana (abreviados)
 * e tipos de regras (permissiva e restritiva).
 *
 * @property context O contexto da aplicação usado para recuperar os recursos de string.
 * @constructor Injeta o contexto da aplicação.
 */

class RuleStringsProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : RuleStringsProvider {

    override fun monday(): String = context.getString(R.string.segunda_abrev)


    override fun tuesday(): String = context.getString(R.string.terca_abrev)


    override fun wednesday(): String = context.getString(R.string.quarta_abrev)


    override fun thursday(): String = context.getString(R.string.quinta_abrev)


    override fun friday(): String = context.getString(R.string.sexta_abrev)


    override fun saturday(): String = context.getString(R.string.sabado_abrev)


    override fun sunday(): String = context.getString(R.string.domingo_abrev)


    override fun permissive(): String = context.getString(R.string.regra_tipo_permissiva)

    override fun restrictive(): String = context.getString(R.string.regra_tipo_restritiva)

    override fun wholeDay(): String = context.getString(R.string.Dia_inteiro)

    override fun everyDay(): String = context.getString(R.string.Todos_os_dias)

}