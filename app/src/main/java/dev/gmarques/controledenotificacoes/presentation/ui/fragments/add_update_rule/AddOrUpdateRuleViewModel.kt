package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule

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
import dev.gmarques.controledenotificacoes.domain.exceptions.InversedRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.OutOfRangeException
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.model.validators.RuleValidator
import dev.gmarques.controledenotificacoes.domain.usecase.rules.AddRuleUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.UpdateRuleUseCase
import dev.gmarques.controledenotificacoes.presentation.EventWrapper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddOrUpdateRuleViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addRuleUseCase: AddRuleUseCase,
    private val updateRuleUseCase: UpdateRuleUseCase,
) : ViewModel() {

    private var editingRule: Rule? = null

    // propriedades do objeto [Rule] que será adicionado
    private var ruleType: RuleType = RuleType.RESTRICTIVE
    private var timeRanges: HashMap<String, TimeRange> = HashMap()
    private var selectedDays: List<WeekDay> = emptyList()
    private var ruleName: String = ""

    private val _ruleTypeLd = MutableLiveData<RuleType>(RuleType.RESTRICTIVE)
    val ruleTypeLd: LiveData<RuleType> = _ruleTypeLd

    private val _timeRangesLd = MutableLiveData<Map<String, TimeRange>>(emptyMap())
    val timeRangesLd: LiveData<Map<String, TimeRange>> = _timeRangesLd

    private val _selectedDaysLd = MutableLiveData<List<WeekDay>>(emptyList())
    val selectedDaysLd: LiveData<List<WeekDay>> = _selectedDaysLd

    private val _ruleNameLd = MutableLiveData("")
    val ruleNameLd: LiveData<String> = _ruleNameLd

    private val _uiEvents = MutableLiveData(UiEvents())
    val uiEvents: LiveData<UiEvents> get() = _uiEvents

    /**
     * Atualiza o tipo de regra atual e notifica os observadores do LiveData.
     *
     * @param type O novo [RuleType] a ser definido.
     */
    fun updateRuleType(type: RuleType) {
        ruleType = type
        _ruleTypeLd.postValue(ruleType)
    }

    /**
     * Adiciona um intervalo de tempo à coleção interna e atualiza a LiveData associada.
     *
     * @param range O intervalo de tempo (TimeRange) a ser adicionado.
     */
    private fun addTimeRange(range: TimeRange) {

        timeRanges[range.id] = range
        _timeRangesLd.postValue(timeRanges.toMap())
    }

    /**
     * Remove um intervalo de tempo.
     *
     * Remove o intervalo de tempo especificado da lista, utilizando o ID e atualiza a Livedata associada.
     *
     * @param range O intervalo de tempo a ser removido.
     */
    fun removeTimeRange(range: TimeRange) {

        timeRanges.remove(range.id)
        _timeRangesLd.postValue(timeRanges.toMap())
    }

    /**
     * Atualiza a lista de dias selecionados e notifica os observadores.
     *
     * @param days Nova lista de `WeekDay` representando os dias selecionados.
     * @throws IllegalArgumentException Se a lista `days` for nula.
     */
    fun updateSelectedDays(days: List<WeekDay>) {
        selectedDays = days
        _selectedDaysLd.postValue(selectedDays)
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
        ruleName = name
        _ruleNameLd.postValue(ruleName)
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

            val errorMessage = when (validationResult.exceptionOrNull()) {
                is OutOfRangeException -> context.getString(R.string.O_intervalo_selecionado_era_inv_lido)
                is InversedRangeException -> context.getString(R.string.O_final_do_intervalo_deve_ser_maior_que_o_inicio)
                else -> throw IllegalStateException("Exceção não prevista. Isso é um bug!")
            }

            _uiEvents.postValue(
                event.copy(
                    simpleErrorMessageEvent = EventWrapper(errorMessage)
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

    /**
     * Salva uma regra, adicionando-a ou atualizando-a.
     *
     * Se `editingRule` for nulo, adiciona uma nova regra. Caso contrário, atualiza a regra existente.
     * Após a operação, navega para a tela inicial.
     *
     * @param rule A regra [Rule] a ser salva.
     */
    private fun saveRule(rule: Rule) = viewModelScope.launch(IO) {

        if (editingRule == null) addRuleUseCase(rule)
        else updateRuleUseCase(rule.copy(id = editingRule!!.id))

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