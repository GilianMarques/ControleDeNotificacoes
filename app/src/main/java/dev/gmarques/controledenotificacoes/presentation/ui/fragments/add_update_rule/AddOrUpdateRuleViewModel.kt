package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule

import TimeRangeValidator
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.exceptions.BlankNameException
import dev.gmarques.controledenotificacoes.domain.exceptions.DuplicateTimeRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.IntersectedRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.InvalidTimeRangeValueException
import dev.gmarques.controledenotificacoes.domain.exceptions.InversedRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.OutOfRangeException
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.model.validators.RuleValidator
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmsOnRuleEditUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.AddRuleUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.UpdateRuleUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
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


    private val _ruleType = MutableStateFlow(RuleType.RESTRICTIVE)
    val ruleType: StateFlow<RuleType> = _ruleType

    private val _ruleName = MutableStateFlow("")
    val ruleName: StateFlow<String> = _ruleName

    private val _selectedDays = MutableStateFlow<List<WeekDay>>(emptyList())
    val selectedDays: StateFlow<List<WeekDay>> = _selectedDays

    private val _timeRanges = MutableStateFlow(LinkedHashMap<String, TimeRange>())
    val timeRanges: StateFlow<LinkedHashMap<String, TimeRange>> = _timeRanges

    private val _eventsFlow = MutableSharedFlow<Event>(replay = 1)
    val eventsFlow: SharedFlow<Event> get() = _eventsFlow


    /**
     * Atualiza o tipo de regra atual e notifica os observadores do LiveData.
     *
     * @param type O novo [RuleType] a ser definido.
     */
    fun updateRuleType(type: RuleType) {
        _ruleType.tryEmit(type)
    }

    /**
     * Adiciona um intervalo de tempo à coleção interna e atualiza a LiveData associada.
     *
     * @param range O intervalo de tempo (TimeRange) a ser adicionado.
     */
    private fun addTimeRange(range: TimeRange) {
        val ranges = LinkedHashMap(timeRanges.value)
        ranges[range.id] = range
        _timeRanges.tryEmit(ranges)
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
        _timeRanges.tryEmit(ranges)
    }

    /**
     * Atualiza a lista de dias selecionados e notifica os observadores.
     *
     * @param days Nova lista de `WeekDay` representando os dias selecionados.
     * @throws IllegalArgumentException Se a lista `days` for nula.
     */
    fun updateSelectedDays(days: List<WeekDay>) {
        _selectedDays.tryEmit(days)
    }

    /**
     * Atualiza o nome da regra (`ruleName`) e notifica os observadores de `_ruleNameLd` com o novo valor.
     *
     * Este méto-do atualiza a propriedade interna `ruleName` e emite o novo valor através do LiveData `_ruleNameLd`,
     * garantindo que qualquer parte do código observando essa LiveData seja informada da mudança.
     *
     * @param name O novo nome da regra.
     * @see ruleName
     * @see _ruleNameLd
     */
    private fun updateRuleName(name: String) {
        _ruleName.tryEmit(name)
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
        return _timeRanges.value.size < RuleValidator.MAX_RANGES
    }

    /**
     * Valida um [TimeRange] individualmente e caso seja um objeto válido
     * chama a funçao responsavel por validar to_do o conjunto de TimeRanges do objeto regra
     * para só entao, caso o objeto seja valido por si só e como parte de uma lista de outros objetos
     * ser adicionao efetivamente a lista de TimeRanges do objeto regra.*/
    fun validateRange(range: TimeRange): Result<TimeRange> {

        val validationResult = TimeRangeValidator.validate(range)

        if (validationResult.isFailure) {

            val errorMessage = when (validationResult.exceptionOrNull()) {
                is OutOfRangeException -> context.getString(R.string.O_intervalo_selecionado_era_inv_lido)
                is InversedRangeException -> context.getString(R.string.O_final_do_intervalo_deve_ser_maior_que_o_inicio)
                else -> throw IllegalStateException("Exceção não prevista. Isso é um bug!")
            }

            _eventsFlow.tryEmit(Event.SimpleErrorMessage(errorMessage))
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

        val ranges = _timeRanges.value.values + range
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

            is InvalidTimeRangeValueException -> context.getString(
                R.string.Voc_definiu_um_valor_inv_lido_para_o_intervalo_de_tempo,
                exception.actual
            )

            is DuplicateTimeRangeException -> context.getString(R.string.Nao_e_possivel_adicionar_um_intervalo_de_tempo_duplicado)
            is IntersectedRangeException -> context.getString(R.string.Nao_sao_permitidos_intervalos_de_tempo_que_se_interseccionam)
            is InversedRangeException -> context.getString(R.string.O_final_do_intervalo_deve_ser_maior_que_o_inicio)
            else -> throw exception
        }

        _eventsFlow.tryEmit(Event.SimpleErrorMessage(message))

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

        val ruleName = _ruleName.value
        val selectedDays = _selectedDays.value
        val timeRanges = _timeRanges.value
        val ruleType = _ruleType.value

        if (validateName(ruleName).isFailure) return
        if (validateDays(selectedDays).isFailure) return
        if (validateRanges(timeRanges.map { it.value }).isFailure) return

        val rule = Rule(
            name = ruleName,
            ruleType = ruleType,
            days = selectedDays,
            timeRanges = timeRanges.values.toList()
        )

        // updateRuleSe

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
        _eventsFlow.tryEmit(Event.SetResultAndClose(validatedRule))
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
        _eventsFlow.tryEmit(Event.SetResultAndClose(rule))
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
    fun validateDays(days: List<WeekDay>): Result<List<WeekDay>> {

        val result = RuleValidator.validateDays(days)

        if (result.isFailure) {
            when (result.getOrNull()) {

                else -> {
                    viewModelScope.launch {
                        delay(200)
                        if (_selectedDays.value.isEmpty()) _eventsFlow.tryEmit(Event.SimpleErrorMessage(context.getString(R.string.Selecione_pelo_menos_um_dia_da_semana)))
                    }
                }
            }
        }
        return result
    }

    /**
     * Valida o nome fornecido usando [RuleValidator.validateName].
     *
     * Se o nome for válido, atualiza o nome da regra com [updateRuleName].
     * Se inválido, envia uma mensagem de erro para [_uiEvents].
     *
     * @param name O nome a ser validado.
     * @return Um [Result] contendo o nome validado (sucesso) ou uma exceção (falha).
     *   - Sucesso: [Result.getOrThrow] retorna o nome (String).
     *   - Falha: [Result.exceptionOrNull] retorna [BlankNameException] ou [OutOfRangeException].
     * @throws IllegalStateException Se a validação falhar com uma exceção não tratada.
     *
     * Erros:
     * - [BlankNameException]: Nome em branco. Envia "O nome não pode ficar em branco" para [_uiEvents].
     * - [OutOfRangeException]: Tamanho do nome fora do intervalo. Envia "O nome deve ter entre {minLength} e {maxLength} caracteres" para [_uiEvents].
     * - Outras Exceções: Lança [IllegalStateException] para exceções não previstas.
     */
    fun validateName(name: String): Result<String> {

        val result = RuleValidator.validateName(name)

        if (result.isSuccess) {
            updateRuleName(result.getOrThrow())
        } else {

            when (val exception = result.exceptionOrNull()) {

                is BlankNameException -> {
                    _eventsFlow.tryEmit(
                        Event.NameErrorMessage(
                            context.getString(R.string.O_nome_n_o_pode_ficar_em_branco)
                        )
                    )
                }

                is OutOfRangeException -> {
                    _eventsFlow.tryEmit(
                        Event.NameErrorMessage(
                            context.getString(
                                R.string.O_nome_deve_ter_entre_e_caracteres, exception.minLength, exception.maxLength
                            )
                        )
                    )
                }

                else -> throw IllegalStateException("Exceção não tratada $exception")
            }
        }

        return result
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