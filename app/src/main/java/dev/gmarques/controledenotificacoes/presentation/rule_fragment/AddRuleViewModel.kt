package dev.gmarques.controledenotificacoes.presentation.rule_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType

class AddRuleViewModel : ViewModel() {


    private val timeIntervals = HashMap<String, TimeInterval>()

    var ruleType: RuleType? = null


    private val _timeIntervalsLiveData = MutableLiveData<HashMap<String, TimeInterval>>()
    val timeIntervalsLiveData: LiveData<HashMap<String, TimeInterval>> get() = _timeIntervalsLiveData


    fun addRule(rule: Rule) {
        // Adicionar a regra ao repositório ou fazer outra ação necessária
        // Você pode invocar o repositório para salvar a regra no banco de dados, por exemplo
    }

    fun addTimeInterval(interval: TimeInterval) {
        timeIntervals += (interval.id to interval)
        _timeIntervalsLiveData.postValue(timeIntervals)
    }

    fun removeTimeIntervals(interval: TimeInterval) {
        timeIntervals.remove(interval.id)
        _timeIntervalsLiveData.postValue(timeIntervals)
    }


}
