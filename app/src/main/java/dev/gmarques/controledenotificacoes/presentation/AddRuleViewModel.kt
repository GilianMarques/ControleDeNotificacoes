package dev.gmarques.controledenotificacoes.presentation

import androidx.lifecycle.ViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule

class AddRuleViewModel : ViewModel() {
    fun addRule(rule: Rule) {
        // Adicionar a regra ao repositório ou fazer outra ação necessária
        // Você pode invocar o repositório para salvar a regra no banco de dados, por exemplo
    }
}
