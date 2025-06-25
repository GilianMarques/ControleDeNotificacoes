package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_condition

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.CantBeNullException
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.BlankKeywordException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.EmptyKeywordsException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.EmptyValidKeywordsException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.InvalidKeywordLengthException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.MaxKeywordsExceededException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.PartialKeywordValidationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class AddOrUpdateConditionViewModel @Inject constructor() : ViewModel() {

    var ruleTypeRestrictive = false


    private val _eventsChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow: Flow<Event> get() = _eventsChannel.receiveAsFlow()

    private val _keywordsFlow = MutableStateFlow<List<String>>(emptyList())
    val keywordsFlow: StateFlow<List<String>> get() = _keywordsFlow


    fun setEditingCondition(condition: Condition) {

    }

    fun addKeywords(keywords: List<String>) {
        val validKeywords = validateKeywordsResult(keywords)
        validKeywords?.let { _keywordsFlow.tryEmit(it) }
    }

    private fun validateKeywordsResult(keywords: List<String>): List<String>? {

        val result = ConditionValidator.validateKeywords(_keywordsFlow.value + keywords)

        if (result.isSuccess) return result.getOrThrow()

        fun notify(message: String) {
            _eventsChannel.trySend(Event.Error(message))
        }
        when (val baseException = result.exceptionOrNull()) {
            is BlankKeywordException -> notify("Não é possível adicionar uma palavra vazia")
            is EmptyKeywordsException -> notify("A lista de palavras-chave não pode estar vazia")
            is EmptyValidKeywordsException -> notify("Nenhuma palavra-chave válida encontrada na lista")
            is InvalidKeywordLengthException -> notify("Palavra-chave com comprimento inválido (${baseException.length}) máx. ${ConditionValidator.MAX_VALUE_LENGTH}")
            is MaxKeywordsExceededException -> notify("Número máximo de palavras-chave aceito é ${baseException.max}, atual ${baseException.found}")
            is PartialKeywordValidationException -> {

                notify("Algumas palavras-chave eram inválidas e não foram adicionadas")
                return baseException.validValues

            }

            null -> throw CantBeNullException()
        }


        return null
    }


    fun removeKeyword(keyword: String) {
        _keywordsFlow.tryEmit(_keywordsFlow.value - keyword)
    }

}

/**
 * Criado por Gilian Marques
 * Em 25/06/2025 as 15:26
 *
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 *
 */
sealed class Event {
    data class Error(val msg: String) : Event()
}