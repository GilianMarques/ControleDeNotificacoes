package dev.gmarques.controledenotificacoes.presentation

import androidx.lifecycle.ViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType

class AddRuleViewModel : ViewModel() {

    val timeIntervals = mutableListOf<TimeInterval>()
    var ruleType: RuleType? = null

    fun addRule(rule: Rule) {
        // Adicionar a regra ao repositório ou fazer outra ação necessária
        // Você pode invocar o repositório para salvar a regra no banco de dados, por exemplo
    }
}
