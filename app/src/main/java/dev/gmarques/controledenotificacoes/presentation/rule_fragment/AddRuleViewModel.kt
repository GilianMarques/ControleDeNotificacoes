package dev.gmarques.controledenotificacoes.presentation.rule_fragment

import TimeRangeValidator
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.exceptions.BlankNameException
import dev.gmarques.controledenotificacoes.domain.exceptions.DuplicateTimeRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.IntersectedRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.OutOfRangeException
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.model.validators.RuleValidator
import dev.gmarques.controledenotificacoes.domain.usecase.AddRuleUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.UpdateRuleUseCase
import dev.gmarques.controledenotificacoes.presentation.EventWrapper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRuleViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addRuleUseCase: AddRuleUseCase,
    private val updateRuleUseCase: UpdateRuleUseCase,
) : ViewModel() {


    private var editingRule: Rule? = null
    private var ruleType: RuleType = RuleType.RESTRICTIVE
    private var timeRanges: HashMap<String, TimeRange> = HashMap()
    private var selectedDays: List<WeekDay> = emptyList()
    private var ruleName: String = ""


    private val _editingRuleLd = MutableLiveData<Rule?>(null)
    val editingRuleLd: LiveData<Rule?> = _editingRuleLd

    private val _ruleTypeLd = MutableLiveData<RuleType>(RuleType.RESTRICTIVE)
    val ruleTypeLd: LiveData<RuleType> = _ruleTypeLd

    private val _timeRangesLd = MutableLiveData<Map<String, TimeRange>>(emptyMap())
    val timeRangesLd: LiveData<Map<String, TimeRange>> = _timeRangesLd

    private val _selectedDaysLd = MutableLiveData<List<WeekDay>>(emptyList())
    val selectedDaysLd: LiveData<List<WeekDay>> = _selectedDaysLd

    private val _ruleNameLd = MutableLiveData<String>("")
    val ruleNameLd: LiveData<String> = _ruleNameLd

    private val _uiEvents = MutableLiveData(UiEvents())
    val uiEvents: LiveData<UiEvents> get() = _uiEvents

    fun updateRuleType(type: RuleType) {
        ruleType = type
        _ruleTypeLd.postValue(ruleType)
    }

    fun addTimeRange(range: TimeRange) {

        timeRanges[range.id] = range
        _timeRangesLd.postValue(timeRanges.toMap())
    }

    fun removeTimeRange(range: TimeRange) {

        timeRanges.remove(range.id)
        _timeRangesLd.postValue(timeRanges.toMap())
    }

    fun updateSelectedDays(days: List<WeekDay>) {
        selectedDays = days
        _selectedDaysLd.postValue(selectedDays)
    }

    fun updateRuleName(name: String) {
        ruleName = name
        _ruleNameLd.postValue(ruleName)
    }

    fun canAddMoreRanges(): Boolean {
        return timeRanges.size < RuleValidator.MAX_RANGES
    }

    /**
     * Valida um [TimeRange] individualmente e caso seja um objeto válido
     * chama a funçao responsavel por validar to_do o conjunto de TimeRanges do objeto regra
     * para só entao, caso o objeto seja valido por si só e como parte de uma lista de outros objetos
     * ser adicionao efetivamente a lista de TimeRanges do objeto regra.*/
    fun validateRange(range: TimeRange): Result<TimeRange> {

        val validationResult = TimeRangeValidator.validate(range)

        if (validationResult.isFailure) {
            val event = _uiEvents.value!!
            _uiEvents.postValue(
                event.copy(
                    simpleErrorMessageEvent = EventWrapper(context.getString(R.string.O_intervalo_selecionado_era_inv_lido))
                )
            )
        }

        return validationResult
    }

    /**
     *Essa função serve para validar se um range recém inserido é compatível com os demais ranges da lista antes de
     * adicionar de fato. Esse funçao deve ser chamada pela camada de UI sempre que um novo range for adicionado.
     *
     * @param range O novo `TimeRange` a ser validado e potencialmente adicionado à sequência.
     * @throws IllegalStateException Se a validação falhar com uma exceção inesperada, ou se não houver exceção quando a validação falha.
     */
    fun validateRangesWithSequenceAndAdd(range: TimeRange): Result<List<TimeRange>> {

        val ranges = timeRanges.values + range
        val result = RuleValidator.validateTimeRanges(ranges)

        if (result.isSuccess) {
            addTimeRange(range)
            return result
        }
        notifyErrorValidatingRanges(result)
        return result
    }

    /**Caso as validaçoes de [validateRangesWithSequenceAndAdd] e [validateRangesWithSequenceAndAdd] falhem
     * essa função etrata o erro e envia uma mensagem pra ui
     * @param result O resultado da validação dos ranges.
     */
    private fun notifyErrorValidatingRanges(result: Result<List<TimeRange>>) {

        val exception = result.exceptionOrNull()
            ?: throw IllegalStateException("Em caso de erro deve haver uma exceção para lançar. Isso é um bug!")

        val message = when (exception) {
            is OutOfRangeException -> {
                if (exception.actual == 0) context.getString(R.string.adicione_pelo_menos_um_intervalo_de_tempo)
                else context.getString(
                    R.string.O_limite_m_ximo_de_intervalos_de_tempo_foi_atingido, RuleValidator.MAX_RANGES
                )
            }

            is DuplicateTimeRangeException -> context.getString(R.string.Nao_e_possivel_adicionar_um_intervalo_de_tempo_duplicado)
            is IntersectedRangeException -> context.getString(R.string.Nao_sao_permitidos_intervalos_de_tempo_que_se_interseccionam)
            else -> throw IllegalStateException("Exceção não prevista. Isso é um bug! ${exception.message}")
        }

        val event = _uiEvents.value!!
        _uiEvents.postValue(event.copy(simpleErrorMessageEvent = EventWrapper(message)))

    }

    fun setEditingRule(rule: Rule) {
        editingRule = rule

        updateRuleName(rule.name)
        updateRuleType(rule.ruleType)
        updateSelectedDays(rule.days)
        rule.timeRanges.forEach { addTimeRange(it) }
    }

    fun validateAndSaveRule() {

        if (validateName(ruleName).isFailure) return
        if (validateDays(selectedDays).isFailure) return
        if (validateRanges(timeRanges.map { it.value }).isFailure) return

        val rule = Rule(
            name = ruleName,
            ruleType = ruleType,
            days = selectedDays,
            timeRanges = timeRanges.values.toList()
        )

        saveRule(rule)
    }

    private fun saveRule(rule: Rule) = viewModelScope.launch(IO) {

        if (editingRule == null) addRuleUseCase.execute(rule)
        else updateRuleUseCase.execute(rule)

        val event = _uiEvents.value!!
        _uiEvents.postValue(event.copy(navigateHomeEvent = EventWrapper(true)))
    }

    /**
     * Valida todos os ranges antes de criar um [Rule]
     * */
    private fun validateRanges(ranges: List<TimeRange>): Result<List<TimeRange>> {
        val result = RuleValidator.validateTimeRanges(ranges)

        if (result.isFailure) {
            notifyErrorValidatingRanges(result)
        }

        return result

    }

    fun validateDays(days: List<WeekDay>): Result<List<WeekDay>> {

        val result = RuleValidator.validateDays(days)

        if (result.isFailure) {
            when (result.getOrNull()) {

                else -> {
                    val event = _uiEvents.value!!
                    _uiEvents.postValue(
                        event.copy(
                            simpleErrorMessageEvent = EventWrapper(
                                context.getString(R.string.Selecione_pelo_menos_um_dia_da_semana)
                            )
                        )
                    )
                }
            }
        }
        return result
    }

    fun validateName(name: String): Result<String> {

        val result = RuleValidator.validateName(name)

        if (result.isSuccess) {
            updateRuleName(result.getOrThrow())
        } else {

            val event = _uiEvents.value!!

            when (val exception = result.exceptionOrNull()) {

                is BlankNameException -> _uiEvents.postValue(
                    event.copy(
                        nameErrorMessageEvent = EventWrapper(
                            context.getString(R.string.O_nome_n_o_pode_ficar_em_branco)
                        )
                    )
                )

                is OutOfRangeException -> _uiEvents.postValue(
                    event.copy(
                        nameErrorMessageEvent = EventWrapper(
                            context.getString(
                                R.string.O_nome_deve_ter_entre_e_caracteres, exception.minLength, exception.maxLength
                            )
                        )
                    )
                )

                else -> throw IllegalStateException("Exceção não tratada $exception")
            }
        }

        return result
    }

}

