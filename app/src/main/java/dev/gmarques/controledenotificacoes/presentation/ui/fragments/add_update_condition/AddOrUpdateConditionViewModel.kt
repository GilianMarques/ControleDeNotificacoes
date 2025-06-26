package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_condition

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
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
class AddOrUpdateConditionViewModel @Inject constructor(@ApplicationContext val context: Context) : ViewModel() {


    private var editingCondition: Condition? = null

    private val _eventsChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow: Flow<Event> get() = _eventsChannel.receiveAsFlow()

    private val _keywordsFlow = MutableStateFlow<List<String>>(emptyList())
    val keywordsFlow: StateFlow<List<String>> get() = _keywordsFlow

    private val _conditionTypeFlow = MutableStateFlow<ConditionType?>(null)
    val conditionTypeFlow: StateFlow<ConditionType?> get() = _conditionTypeFlow

    private val _fieldFlow = MutableStateFlow<NotificationField?>(null)
    val fieldFlow: StateFlow<NotificationField?> get() = _fieldFlow

    private val _conditionDone = MutableStateFlow<Condition?>(null)
    val conditionDone: StateFlow<Condition?> get() = _conditionDone


    private val _caseSensitiveFlow = MutableStateFlow(false)
    val caseSensitiveFlow: StateFlow<Boolean> get() = _caseSensitiveFlow


    fun setEditingCondition(condition: Condition) {

        editingCondition = condition

        _keywordsFlow.tryEmit(condition.keywords)
        _conditionTypeFlow.tryEmit(condition.type)
        _fieldFlow.tryEmit(condition.field)
        _caseSensitiveFlow.tryEmit(condition.caseSensitive)
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
                is BlankKeywordException -> notify(context.getString(R.string.Nao_poss_vel_adicionar_uma_palavra_vazia))
                is InvalidKeywordLengthException -> notify(
                    context.getString(
                        R.string.Palavra_chave_com_comprimento_inv_lido_m_x,
                        baseException.length,
                        ConditionValidator.KEYWORD_MAX_LENGTH
                    )
                )

                null -> throw CantBeNullException()
            }

            return null
        }

        val keywordsListValidationResult =
            ConditionValidator.validateKeywords(_keywordsFlow.value + keywordValidationResult.getOrThrow())

        if (keywordsListValidationResult.isFailure) {
            when (val baseException = keywordsListValidationResult.exceptionOrNull()) {
                is EmptyKeywordsException -> notify(context.getString(R.string.A_lista_de_palavras_chave_n_o_pode_estar_vazia))
                is MaxKeywordsExceededException -> notify(
                    context.getString(
                        R.string.Numero_m_ximo_de_palavras_chave_aceito_atual,
                        baseException.max,
                        baseException.found
                    )
                )

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

    fun validateCondition() {

        val values = listOf(_keywordsFlow, _conditionTypeFlow, _fieldFlow, _caseSensitiveFlow).filter { it.value == null }
        if (values.isNotEmpty()) return


        val condition = Condition(
            _conditionTypeFlow.value!!,
            _fieldFlow.value!!,
            _keywordsFlow.value,
            _caseSensitiveFlow.value
        )

        try {
            ConditionValidator.validate(condition)
        } catch (ex: Exception) {
            Log.e("USUK", "AddOrUpdateConditionViewModel.validateAndSaveCondition: $ex")
            _eventsChannel.trySend(Event.Error(context.getString(R.string.Verifique_os_campos)))
            return
        }

        _conditionDone.tryEmit(condition)

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