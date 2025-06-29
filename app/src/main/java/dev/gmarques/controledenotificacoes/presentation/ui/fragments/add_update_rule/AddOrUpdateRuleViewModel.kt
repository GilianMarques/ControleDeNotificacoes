package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule

import TimeRangeValidator
import TimeRangeValidator.MAX_RANGES
import TimeRangeValidator.TimeRangeValidatorException.AllDayWithNoZeroedValuesException
import TimeRangeValidator.TimeRangeValidatorException.DuplicateTimeRangeException
import TimeRangeValidator.TimeRangeValidatorException.HourOutOfRangeException
import TimeRangeValidator.TimeRangeValidatorException.IntersectedRangeException
import TimeRangeValidator.TimeRangeValidatorException.InversedRangeException
import TimeRangeValidator.TimeRangeValidatorException.MinuteOutOfRangeException
import TimeRangeValidator.TimeRangeValidatorException.RangesOutOfRangeException
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.CantBeNullException
import dev.gmarques.controledenotificacoes.domain.OperationResult
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.RuleValidatorException
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.RuleValidatorException.BlankIdException
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.RuleValidatorException.DaysOutOfRangeException
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.RuleValidatorException.NameOutOfRangeException
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.RuleValidatorException.TimeRangeValidationException
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmsOnRuleEditUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.AddRuleUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.UpdateRuleUseCase
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule.Event.NameErrorMessage
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule.Event.SimpleErrorMessage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddOrUpdateRuleViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addRuleUseCase: AddRuleUseCase,
    private val updateRuleUseCase: UpdateRuleUseCase,
    private val rescheduleAlarmsOnRuleEditUseCase: RescheduleAlarmsOnRuleEditUseCase,
) : ViewModel() {

    private var editingRule: Rule? = null

    private val _ruleTypeFlow = MutableStateFlow(RuleType.RESTRICTIVE)
    val ruleType: StateFlow<RuleType> = _ruleTypeFlow

    private val _ruleNameFlow = MutableStateFlow("")
    val ruleName: StateFlow<String> = _ruleNameFlow

    private val _selectedDaysFlow = MutableStateFlow<List<WeekDay>>(emptyList())
    val selectedDays: StateFlow<List<WeekDay>> = _selectedDaysFlow

    private val _conditionFlow = MutableStateFlow<Condition?>(null)
    val conditionFlow: StateFlow<Condition?> = _conditionFlow

    private val _timeRangesFlow = MutableStateFlow(LinkedHashMap<String, TimeRange>())
    val timeRanges: StateFlow<LinkedHashMap<String, TimeRange>> = _timeRangesFlow

    private val _eventsChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow: Flow<Event> get() = _eventsChannel.receiveAsFlow()

    /**
     * Atualiza o tipo de regra atual e notifica os observadores do LiveData.
     *
     * @param type O novo [RuleType] a ser definido.
     */
    fun updateRuleType(type: RuleType) {
        _ruleTypeFlow.tryEmit(type)
    }

    /**
     * Adiciona um intervalo de tempo à coleção interna e atualiza a LiveData associada.
     *
     * @param range O intervalo de tempo (TimeRange) a ser adicionado.
     */
    private fun addTimeRange(range: TimeRange) {
        val ranges = LinkedHashMap(timeRanges.value)
        ranges[range.id] = range
        _timeRangesFlow.tryEmit(ranges)
    }

    /**
     * Remove um intervalo de tempo.
     *
     * Remove o intervalo de tempo especificado da lista, utilizando o ID e atualiza a Livedata associada.
     *
     * @param range O intervalo de tempo a ser removido.
     */
    fun deleteTimeRange(range: TimeRange) {
        val ranges = LinkedHashMap(timeRanges.value)
        ranges.remove(range.id)
        _timeRangesFlow.tryEmit(ranges)
    }

    /**
     * Atualiza a lista de dias selecionados e notifica os observadores.
     *
     * @param days Nova lista de `WeekDay` representando os dias selecionados.
     * @throws IllegalArgumentException Se a lista `days` for nula.
     */
    fun updateSelectedDays(days: List<WeekDay>) {
        _selectedDaysFlow.tryEmit(days)
    }

    /**
     * Atualiza a condição atual da regra.
     *
     * Emite a nova condição (que pode ser nula) para o `_conditionFlow`,
     * notificando quaisquer observadores sobre a mudança.
     * @param condition A [Condition] a ser definida, ou `null`
     */
    private fun updateCondition(condition: Condition?) {
        _conditionFlow.tryEmit(condition)
    }

    /**
     * Atualiza o nome da regra (`ruleName`) e notifica os observadores de `_ruleNameLd` com o novo valor.
     *
     * Este méto-do atualiza a propriedade interna `ruleName` e emite o novo valor através do LiveData `_ruleNameLd`,
     * garantindo que qualquer parte do código observando essa LiveData seja informada da mudança.
     *
     * @param name O novo nome da regra.
     * @see ruleName
     */
    private fun updateRuleName(name: String) {
        _ruleNameFlow.tryEmit(name)
    }

    /**
     * Verifica se o usuário pode adicionar mais intervalos de tempo, conforme as regras de negócio.
     *
     * A função retorna `true` se o número atual de intervalos em `timeRanges` for menor que o máximo
     * permitido definido por `RuleValidator.MAX_RANGES`; caso contrário, retorna `false`.
     *
     * @return `true` se o usuário pode adicionar mais intervalos, `false` caso contrário.
     */
    fun canAddMoreRanges(): Boolean {
        return _timeRangesFlow.value.size < MAX_RANGES
    }

    /**
     *Essa função serve para validar se um range recém inserido é compatível com os demais ranges da lista antes de
     * adicionar de fato. Esse funçao deve ser chamada pela camada de UI sempre que um novo range for adicionado.
     *
     * @param range O novo `TimeRange` a ser validado e potencialmente adicionado à sequência.
     * @throws IllegalStateException Se a validação falhar com uma exceção inesperada, ou se não houver exceção quando a validação falha.
     */
    fun validateRangesWithSequenceAndAdd(range: TimeRange): OperationResult<TimeRangeValidator.TimeRangeValidatorException, List<TimeRange>> {

        val ranges = _timeRangesFlow.value.values + range
        val result = TimeRangeValidator.validateTimeRanges(ranges)

        if (result.isSuccess) addTimeRange(range)
        else notifyErrorValidatingRanges(result)


        return result
    }

    /**Caso as validaçoes de [validateRangesWithSequenceAndAdd] e [validateRanges] falhem
     * essa função etrata o erro e envia uma mensagem pra ui
     * @param result O resultado da validação dos ranges.
     */
    private fun notifyErrorValidatingRanges(result: OperationResult<TimeRangeValidator.TimeRangeValidatorException, List<TimeRange>>) {

        val exception = result.exceptionOrNull()


        val message = when (exception) {
            is AllDayWithNoZeroedValuesException -> throw exception // só vai acontecer por erro de programaçao
            is DuplicateTimeRangeException -> context.getString(R.string.Nao_e_possivel_adicionar_um_intervalo_de_tempo_duplicado)
            is HourOutOfRangeException -> context.getString(R.string.Valor_da_hora_inv_lido, exception.actualHour)
            is IntersectedRangeException -> context.getString(R.string.Nao_sao_permitidos_intervalos_de_tempo_que_se_interseccionam)
            is InversedRangeException -> context.getString(R.string.O_final_do_intervalo_deve_ser_maior_que_o_inicio)
            is MinuteOutOfRangeException -> context.getString(R.string.Valor_do_minuto_inv_lido, exception.actualMinute)
            is RangesOutOfRangeException -> {
                if (exception.actual == 0) context.getString(R.string.adicione_pelo_menos_um_intervalo_de_tempo)
                else context.getString(
                    R.string.O_limite_m_ximo_de_intervalos_de_tempo_foi_atingido, MAX_RANGES
                )
            }

            null -> throw CantBeNullException()
        }

        _eventsChannel.trySend(SimpleErrorMessage(message))

    }

    /**
     * Define a regra atual para edição e atualiza a interface com as propriedades da regra.
     *
     * @param rule A regra [Rule] a ser definida para edição.
     */
    fun setEditingRule(rule: Rule) {
        editingRule = rule

        updateRuleName(rule.name)
        updateRuleType(rule.ruleType)
        updateSelectedDays(rule.days)
        updateCondition(rule.condition)
        viewModelScope.launch {
            rule.timeRanges.forEach {
                addTimeRange(it)
            }
        }
    }

    /**
     * Valida e salva uma regra.
     *
     * Esta função verifica se o nome, os dias selecionados e os intervalos de tempo são válidos.
     * Se todos forem válidos, uma nova `Rule` é criada e salva.
     * Caso contrário, a função retorna sem salvar.
     *
     * Validações:
     *   - `ruleName`: Usando `validateName`.
     *   - `selectedDays`: Usando `validateDays`.
     *   - `timeRanges`: Usando `validateRanges` (aplicado aos valores do mapa).
     *
     */
    fun validateAndSaveRule() {

        val ruleName = _ruleNameFlow.value
        val selectedDays = _selectedDaysFlow.value
        val timeRanges = _timeRangesFlow.value
        val ruleType = _ruleTypeFlow.value
        val condition = _conditionFlow.value

        if (validateName(ruleName).isFailure) return
        if (validateDays(selectedDays).isFailure) return
        if (validateRanges(timeRanges.map { it.value }).isFailure) return

        val rule = Rule(
            name = ruleName,
            ruleType = ruleType,
            days = selectedDays,
            condition = condition,
            timeRanges = timeRanges.values.toList()
        )

        if (editingRule == null) saveRule(rule)
        else updateRule(rule)
    }

    /**
     * Salva uma nova regra no banco de dados e emite um evento para fechar a tela.
     *
     * Esta função é chamada quando uma nova regra é criada e validada com sucesso.
     * Ela utiliza o `addRuleUseCase` para persistir a regra no banco de dados
     * em uma corrotina no dispatcher IO. Após a conclusão da operação,
     * emite um evento `Event.SetResultAndClose` contendo a regra salva,
     * sinalizando para a UI que a operação foi bem-sucedida e a tela pode ser fechada.
     *
     * @param validatedRule A regra [Rule] que foi validada e está pronta para ser salva.
     */
    private fun saveRule(validatedRule: Rule) = viewModelScope.launch(IO) {
        addRuleUseCase(validatedRule)
        _eventsChannel.trySend(Event.SetResultAndClose(validatedRule))
    }

    /**
     * Atualiza uma regra existente no banco de dados e emite um evento para fechar a tela.
     *
     * Esta função é chamada quando uma regra existente está sendo editada e as alterações
     * foram validadas com sucesso. Ela cria uma cópia da `validatedRule` com o ID
     * da `editingRule` original (para garantir que a regra correta seja atualizada).
     * Em seguida, utiliza o `updateRuleUseCase` para persistir as alterações no banco de dados
     * em uma corrotina no dispatcher IO. Após a conclusão da operação,
     * emite um evento `Event.SetResultAndClose` contendo a regra atualizada,
     * sinalizando para a UI que a operação foi bem-sucedida e a tela pode ser fechada.
     *
     * @param validatedRule A regra [Rule] com as alterações validadas, pronta para ser atualizada.
     */
    private fun updateRule(validatedRule: Rule) = viewModelScope.launch(IO) {
        val rule = validatedRule.copy(id = editingRule!!.id)
        updateRuleUseCase(rule)
        rescheduleAlarmsOnRuleEditUseCase(rule)
        _eventsChannel.trySend(Event.SetResultAndClose(rule))
    }

    /**
     * Valida todos os ranges antes de criar um [Rule]
     * */
    private fun validateRanges(ranges: List<TimeRange>): OperationResult<TimeRangeValidator.TimeRangeValidatorException, List<TimeRange>> {
        val result = TimeRangeValidator.validateTimeRanges(ranges)

        if (result.isFailure) notifyErrorValidatingRanges(result)

        return result

    }

    /**
     * Valida uma lista de objetos WeekDay.
     *
     * Utiliza [RuleValidator.validateDays] para verificar a validade dos dias.
     * Em caso de falha, envia um evento UI para exibir uma mensagem de erro.
     *
     * @param days A lista de [WeekDay] a ser validada.
     * @return Um [Result] contendo:
     *         - Sucesso: A lista de [WeekDay] se a validação passar.
     *         - Falha: Uma exceção, se a validação falhar. O tipo da exceção é determinado por [RuleValidator.validateDays].
     *           Nesse caso, um evento de erro [EventWrapper] é enviado para `_uiEvents`.
     * @throws 'Qualquer exceção lançada por [RuleValidator.validateDays].
     *
     * @see RuleValidator.validateDays
     * @see WeekDay
     * @see Result
     */
    fun validateDays(days: List<WeekDay>): OperationResult<RuleValidatorException, List<WeekDay>> {

        val result = RuleValidator.validateDays(days)

        if (result.isFailure) when (val exception = result.exceptionOrNull()) {

            is DaysOutOfRangeException -> viewModelScope.launch {
                delay(200)
                if (_selectedDaysFlow.value.isEmpty()) _eventsChannel.trySend(SimpleErrorMessage(context.getString(R.string.Selecione_pelo_menos_um_dia_da_semana)))
            }

            is BlankIdException -> throw exception
            is TimeRangeValidationException -> throw exception
            is NameOutOfRangeException -> throw exception
            is RuleValidatorException.ConditionValidationException -> throw exception
            null -> throw CantBeNullException()
        }

        return result
    }

    /**
     * Valida o nome de uma regra.
     *
     * @param name O nome a ser validado.
     * @return [OperationResult] indicando sucesso com o nome validado ou falha com [RuleValidator.RuleValidatorException].
     * Em caso de sucesso, atualiza o nome da regra internamente.
     * Em caso de falha do tipo [NameOutOfRangeException], envia um evento de erro para a UI.
     */
    fun validateName(name: String): OperationResult<RuleValidatorException, String> {

        val result = RuleValidator.validateName(name)

        if (result.isSuccess) {
            updateRuleName(result.getOrThrow())
        } else when (val exception = result.exceptionOrNull()) {
            is NameOutOfRangeException -> {
                val errorMessage = context.getString(
                    R.string.O_nome_deve_ter_entre_e_caracteres, exception.minLength, exception.maxLength
                )
                _eventsChannel.trySend(NameErrorMessage(errorMessage))
            }

            is BlankIdException -> throw exception
            is DaysOutOfRangeException -> throw exception
            is TimeRangeValidationException -> throw exception
            is RuleValidatorException.ConditionValidationException -> throw exception
            null -> throw CantBeNullException()

        }


        return result
    }

    fun setCondition(condition: Condition) {
        _conditionFlow.tryEmit(condition)
    }

    fun removeCondition() {
        _conditionFlow.value = null
    }
}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class Event {
    data class SetResultAndClose(val data: Rule) : Event()
    data class SimpleErrorMessage(val data: String) : Event()
    data class NameErrorMessage(val data: String) : Event()
}