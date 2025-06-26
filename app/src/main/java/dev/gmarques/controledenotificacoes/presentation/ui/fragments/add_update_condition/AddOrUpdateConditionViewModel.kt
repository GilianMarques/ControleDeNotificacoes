package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_condition

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.CantBeNullException
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.KeywordsValidationException.EmptyKeywordsException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.KeywordsValidationException.MaxKeywordsExceededException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.SingleKeywordValidationException.BlankKeywordException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.SingleKeywordValidationException.InvalidKeywordLengthException
import dev.gmarques.controledenotificacoes.domain.model.enums.ConditionType
import dev.gmarques.controledenotificacoes.domain.model.enums.NotificationField
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

    private val _conditionTypeFlow = MutableStateFlow<ConditionType?>(null)
    val conditionTypeFlow: StateFlow<ConditionType?> get() = _conditionTypeFlow

    private val _fieldFlow = MutableStateFlow<NotificationField?>(null)
    val fieldFlow: StateFlow<NotificationField?> get() = _fieldFlow

    private val _caseSensitiveFlow = MutableStateFlow<Boolean?>(null)
    val caseSensitiveFlow: StateFlow<Boolean?> get() = _caseSensitiveFlow

    fun setEditingCondition(condition: Condition) {

    }

    fun addKeyword(keyword: String) {
        val validKeywords = validateKeywordsResult(keyword)
        validKeywords?.let { _keywordsFlow.tryEmit(it) }
    }

    private fun validateKeywordsResult(keyword: String): List<String>? {

        fun notify(message: String) {
            _eventsChannel.trySend(Event.Error(message))
        }

        val keywordValidationResult = ConditionValidator.validateKeyword(keyword)

        if (keywordValidationResult.isFailure) {
            when (val baseException = keywordValidationResult.exceptionOrNull()) {
                is BlankKeywordException -> notify("Não é possível adicionar uma palavra vazia")
                is InvalidKeywordLengthException -> notify("Palavra-chave com comprimento inválido (${baseException.length}) máx. ${ConditionValidator.KEYWORD_MAX_LENGTH}")
                null -> throw CantBeNullException()
            }

            return null
        }

        val keywordsListValidationResult =
            ConditionValidator.validateKeywords(_keywordsFlow.value + keywordValidationResult.getOrThrow())

        if (keywordsListValidationResult.isFailure) {
            when (val baseException = keywordsListValidationResult.exceptionOrNull()) {
                is EmptyKeywordsException -> notify("A lista de palavras-chave não pode estar vazia")
                is MaxKeywordsExceededException -> notify("Número máximo de palavras-chave aceito é ${baseException.max}, atual ${baseException.found}")
                null -> throw CantBeNullException()
            }
            return null

        } else return keywordsListValidationResult.getOrThrow()

    }

    fun removeKeyword(keyword: String) {
        _keywordsFlow.tryEmit(_keywordsFlow.value - keyword)
    }

    fun setConditionType(type: ConditionType) {
        _conditionTypeFlow.tryEmit(type)
    }

    fun setField(field: NotificationField) {
        _fieldFlow.tryEmit(field)
    }

    fun setCaseSensitive(checked: Boolean) {
        _caseSensitiveFlow.tryEmit(checked)
    }

    fun validateAndSaveCondition() {

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