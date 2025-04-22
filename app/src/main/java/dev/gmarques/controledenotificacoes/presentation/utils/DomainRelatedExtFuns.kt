package dev.gmarques.controledenotificacoes.presentation.utils

import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType

/**
 * Criado por Gilian Marques
 * Em terça-feira, 22 de abril de 2025 as 20:26.
 * Aqui ficam as funçoes de extençao relacionadas ao modelos de objetos de dominio dos quais a aplicação depende
 */
object DomainRelatedExtFuns {

    fun Rule.getAdequateIconReference() = when (this.ruleType) {
        RuleType.RESTRICTIVE -> R.drawable.vec_rule_restrictive
        RuleType.PERMISSIVE -> R.drawable.vec_rule_permissive
    }
}