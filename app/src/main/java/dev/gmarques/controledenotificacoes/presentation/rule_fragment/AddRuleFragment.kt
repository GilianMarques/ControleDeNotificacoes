package dev.gmarques.controledenotificacoes.presentation.rule_fragment

import TimeRangeValidator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentAddRuleBinding
import dev.gmarques.controledenotificacoes.databinding.ItemIntervalBinding
import dev.gmarques.controledenotificacoes.domain.exceptions.BlankNameException
import dev.gmarques.controledenotificacoes.domain.exceptions.DuplicateTimeRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.IntersectedRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.OutOfRangeException
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.model.validators.RuleValidator
import dev.gmarques.controledenotificacoes.domain.plataform.VibratorInterface
import dev.gmarques.controledenotificacoes.domain.utils.TimeRangeExtensionFun.endIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.utils.TimeRangeExtensionFun.startIntervalFormatted
import dev.gmarques.controledenotificacoes.plataform.VibratorImpl
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.addViewWithTwoStepsAnimation
import javax.inject.Inject

@AndroidEntryPoint
class AddRuleFragment : Fragment() {

    @Inject
    lateinit var vibrator: VibratorInterface
    private val viewModel: AddRuleViewModel by viewModels()
    private lateinit var binding: FragmentAddRuleBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddRuleBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupNameInput()
        setupButtonTypeRule()
        setupChipDays()
        setupButtonAddInterval()
        setupFabAddRule()
        observeStateChanges()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupFabAddRule() = with(binding) {
        fabAdd.setOnClickListener(AnimatedClickListener {
            edtName.clearFocus()
        })
    }

    private fun setupNameInput() = with(binding) {
        edtName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateName(edtName.text.toString())
            }

        }
    }

    private fun validateName(name: String) {
        val result = RuleValidator.validateName(name)
        if (result.isSuccess) {
            viewModel.updateRuleName(result.getOrThrow())
        } else {
            when (val exception = result.exceptionOrNull()) {
                is BlankNameException -> {
                    showErrorSnackBar(getString(R.string.O_nome_n_o_pode_ficar_em_branco))
                    binding.edtName.error = getString(R.string.O_nome_n_o_pode_ficar_em_branco)
                }

                is OutOfRangeException -> {
                    showErrorSnackBar(
                        getString(
                            R.string.O_nome_deve_ter_entre_e_caracteres,
                            exception.minLength,
                            exception.maxLength
                        )
                    )
                }
            }
        }
    }

    /**
     * Configura o comportamento dos chips dentro do `chipGroup` para habilitar uma animação visual quando seu estado de seleção é alterado.
     *
     * Quando o estado de seleção de um chip é modificado, ele é brevemente removido e, em seguida, readicionado ao `chipGroup` na sua posição original.
     * Esse processo dispara uma animação, fornecendo feedback visual ao usuário.
     *
     * Este méto.do itera sobre cada `Chip` do `chipGroup` e associa um `setOnCheckedChangeListener` a ele. O listener executa as seguintes ações:
     *
     * 1. **Obter Índice Original:** Determina o índice atual do chip dentro do `chipGroup`.
     * 2. **Remover Chip:** Remove temporariamente o chip do `chipGroup`.
     * 3. **Readicionar Chip:** Adiciona o chip de volta ao `chipGroup` no índice previamente determinado.
     *
     * Essa operação de remover e readicionar, no mesmo índice, força o layout a ser redesenhado, criando um efeito de animação visual sutil.
     *
     * Pressupostos:
     * - `binding` é um objeto `ViewBinding` válido.
     * - Todas as filhas do `chipGroup` são instâncias de `Chip`.
     */
    private fun setupChipDays() = with(binding) {

        val animateChipCheck = { buttonView: View, index: Int ->
            chipGroup.removeView(buttonView)
            chipGroup.addView(buttonView, index)
            vibrator.interaction()
        }

        for (view in chipGroup.children) {


            (view as Chip).setOnCheckedChangeListener { buttonView, checked ->

                animateChipCheck(buttonView, chipGroup.indexOfChild(buttonView))

                val days = chipGroup.children
                    .filter { (it as Chip).isChecked }
                    .map {
                        when (it.tag.toString().toInt()) {
                            // TODO: tentar otimizar isso
                            WeekDay.SUNDAY.dayNumber -> WeekDay.SUNDAY
                            WeekDay.MONDAY.dayNumber -> WeekDay.MONDAY
                            WeekDay.TUESDAY.dayNumber -> WeekDay.TUESDAY
                            WeekDay.WEDNESDAY.dayNumber -> WeekDay.WEDNESDAY
                            WeekDay.THURSDAY.dayNumber -> WeekDay.THURSDAY
                            WeekDay.FRIDAY.dayNumber -> WeekDay.FRIDAY
                            WeekDay.SATURDAY.dayNumber -> WeekDay.SATURDAY
                            else -> throw IllegalStateException("Dia inválido: ${it.tag}")
                        }
                    }

                viewModel.updateSelectedDays(days.toList())

            }
        }
    }

    private fun setupButtonTypeRule() = with(binding) {
        mbtTypeRule.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            vibrator.interaction()

            if (tvRuleTypeInfo.isGone) tvRuleTypeInfo.visibility = VISIBLE

            when (toggleButton.checkedButtonId) {
                R.id.btn_permissive -> {
                    viewModel.updateRuleType(RuleType.PERMISSIVE)
                    tvRuleTypeInfo.text =
                        getString(R.string.Permite_mostrar_as_notifica_es_nos_dias_e_horarios_selecionados)
                }

                R.id.btn_restritive -> {
                    viewModel.updateRuleType(RuleType.RESTRICTIVE)
                    tvRuleTypeInfo.text =
                        getString(R.string.As_notifica_es_ser_o_bloqueadas_nos_dias_e_hor_rios_selecionados)
                }
            }
        }
    }

    /**
     * Configura o listener de clique para o botão "Adicionar Intervalo" (ivAddInterval).
     *
     * Esta função configura o `ivAddInterval` (presumivelmente um ImageView ou similar) para
     * acionar a função `collectIntervalData()` quando clicado. Ela usa um `AnimatedClickListener`
     * para fornecer feedback visual ao evento de clique.
     *
     * O `AnimatedClickListener` fornece uma animação de escala padrão.
     *
     * @see AnimatedClickListener Para detalhes sobre a animação aplicada ao clique.
     * @see collectIntervalData A função chamada quando o botão é clicado.
     */
    private fun setupButtonAddInterval() = with(binding) {
        ivAddRange.setOnClickListener(AnimatedClickListener {
            vibrator.interaction()
            if ((viewModel.uiState.value?.timeRanges?.size ?: 0) < RuleValidator.MAX_RANGES) {
                collectIntervalData()
            } else {
                showErrorSnackBar(
                    getString(
                        R.string.O_limite_m_ximo_de_intervalos_de_tempo_foi_atingido,
                        RuleValidator.MAX_RANGES
                    )
                )
            }
        })
    }

    /**
     * Coleta e valida os dados de um intervalo de tempo do usuário através de um seletor de tempo (time picker).
     *
     * Esta função orquestra o processo de coleta dos horários de início e fim para um intervalo de tempo.
     * Ela utiliza um seletor de tempo para obter os valores de hora e minuto do usuário. Primeiramente,
     * solicita ao usuário que selecione o horário de início. Após a seleção do horário de início,
     * solicita ao usuário que selecione o horário de fim. Finalmente, após ambos os horários de início
     * e fim terem sido selecionados, ela valida se o intervalo selecionado é válido.
     *
     * Os dados de tempo são armazenados em um array de inteiros `data` com a seguinte estrutura:
     * - `data[0]`: Hora de início (0-23)
     * - `data[1]`: Minuto de início (0-59)
     * - `data[2]`: Hora de fim (0-23)
     * - `data[3]`: Minuto de fim (0-59)
     *
     * A função utiliza duas funções lambda:
     * - `collectEndValues`: Coleta o horário de fim (hora e minuto) usando o seletor de tempo e atualiza os elementos correspondentes no array `data`. Após a coleta do horário de fim, ela chama `validateInterval` para validar o intervalo completo.
     * - `collectStartValues`: Coleta o horário de início (hora e minuto) usando o seletor de tempo e atualiza os elementos correspondentes no array `data`. Após a coleta do horário de início, ela então chama `collectEndValues` para coletar o horário de fim.
     *
     * Finalmente, a função chama `collectStartValues` para iniciar o processo.
     *
     * @throws IllegalStateException Se a função `showTimePicker` não estiver devidamente definida ou implementada na classe circundante.
     * @see showTimePicker
     * @see validateRange
     * @see TimeRange
     */
    private fun collectIntervalData() {
        val data = intArrayOf(8, 0, 18, 0)

        val collectEndValues = {
            showTimePicker(data[2], data[3], false) { hour, minute ->
                data[2] = hour
                data[3] = minute
                validateRange(TimeRange(data[0], data[1], data[2], data[3]))
            }
        }

        val collectStartValues = {
            showTimePicker(data[0], data[1], true) { hour, minute ->
                data[0] = hour
                data[1] = minute
                collectEndValues()
            }
        }

        collectStartValues()
    }

    /**
     * Exibe um diálogo Material Time Picker.
     *
     * Esta função exibe um diálogo de seletor de hora no formato de 24 horas, permitindo que o usuário
     * selecione um horário. Ela também define um título que depende se o usuário está selecionando a hora de início ou de fim.
     *
     * @param hour A hora inicial a ser exibida no seletor de hora.
     * @param minute O minuto inicial a ser exibido no seletor de hora.
     * @param isStartTime Um booleano que indica se este seletor de hora é para selecionar a hora de início (true) ou a hora de fim (false).
     * @param callback Uma função lambda que será chamada quando o usuário confirmar sua seleção.
     *                 Ela recebe dois parâmetros:
     *                 - A hora selecionada (Int).
     *                 - O minuto selecionado (Int).
     *
     * @throws IllegalStateException se a activity for nula. Isso pode acontecer se o fragmento não estiver anexado a uma activity.
     * @sample
     * ```kotlin
     *   showTimePicker(10, 30, true) { horaSelecionada, minutoSelecionado ->
     *       // Lidar com o horário selecionado (horaSelecionada:minutoSelecionado)
     *       Log.d("TimePicker", "Horário Selecionado: $horaSelecionada:$minutoSelecionado")
     *   }
     * ```
     * @see MaterialTimePicker
     * @see TimeFormat
     */
    private fun showTimePicker(
        hour: Int,
        minute: Int,
        isStartTime: Boolean,
        callback: (Int, Int) -> Unit,
    ) {

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(
                if (isStartTime) getString(R.string.Selecione_o_in_cio_do_intervalo_de_tempo) else getString(
                    R.string.Selecione_o_fim_do_intervalo_de_tempo
                )
            )
            .build()

        picker.isCancelable = false

        picker.addOnPositiveButtonClickListener {
            callback(picker.hour, picker.minute)
        }

        activity?.supportFragmentManager?.let { picker.show(it, "TimePicker") }
    }

    /**
     * Valida um [TimeRange] individualmente e caso seja um objeto válido
     * chama a funçao responsavel por validar to_do o conjunto de TimeRanges do objeto regra
     * para só entao, caso o objeto seja valido por si só e como parte de uma lista de outros objetos
     * ser adicionao efetivamente a lista de TimeRanges do objeto regra.*/
    private fun validateRange(range: TimeRange) {

        val validationResult = TimeRangeValidator.validate(range)

        if (validationResult.isSuccess) {
            validateRangesSequence(range)
        } else {
            showErrorSnackBar(getString(R.string.O_intervalo_selecionado_era_inv_lido))
        }
    }

    /**
     * Valida um novo TimeRange em relação à sequência existente de TimeRanges no estado do ViewModel.
     *
     * A função verifica se a adição do novo `range` à lista de TimeRanges existentes é válida,
     * considerando regras como: limite máximo de intervalos, duplicatas e interseções.
     *
     * Se a validação for bem-sucedida, o `range` é adicionado ao ViewModel.
     * Se falhar, uma mensagem de erro específica é exibida ao usuário, identificando a causa da falha.
     *
     * @param range O novo `TimeRange` a ser validado e potencialmente adicionado à sequência.
     * @throws IllegalStateException Se a validação falhar com uma exceção inesperada, ou se não houver exceção quando a validação falha.
     */
    private fun validateRangesSequence(range: TimeRange) {

        val ranges = viewModel.uiState.value?.timeRanges?.values.orEmpty() + range
        val result = RuleValidator.validateTimeRanges(ranges)

        if (result.isSuccess) {
            viewModel.addTimeRange(range)
            return
        }

        val exception = result.exceptionOrNull()
            ?: throw IllegalStateException("Em caso de erro deve haver uma exceção para lançar. Isso é um bug!")

        val messageResId = when (exception) {
            is OutOfRangeException -> R.string.O_limite_m_ximo_de_intervalos_de_tempo_foi_atingido
            is DuplicateTimeRangeException -> R.string.Nao_e_possivel_adicionar_um_intervalo_de_tempo_duplicado
            is IntersectedRangeException -> R.string.Nao_sao_permitidos_intervalos_de_tempo_que_se_interseccionam
            else -> throw IllegalStateException("Exceção não prevista. Isso é um bug! ${exception.message}")
        }

        showErrorSnackBar(getString(messageResId, RuleValidator.MAX_RANGES))
    }

    /**
     * Exibe um Snackbar de erro com a mensagem de erro fornecida e aciona uma vibração como feedback.
     *
     * Esta função é uma utilidade para mostrar mensagens de erro não críticas ao usuário. Ela utiliza
     * o Snackbar do Android para uma exibição temporária da mensagem e a combina com uma breve vibração
     * para fornecer feedback adicional.
     *
     * @param errorMsg A mensagem de erro a ser exibida no Snackbar. Esta deve ser uma string concisa
     *                 explicando a natureza do erro ao usuário.
     *
     * @see Snackbar
     * @see VibratorImpl
     */
    private fun showErrorSnackBar(errorMsg: String) {
        Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
        vibrator.error()
    }

    private fun observeStateChanges() {
        viewModel.uiState.observe(viewLifecycleOwner) {

            with(it.timeRanges) {
                manageIntervalViews(this)
            }

            with(it.ruleName) {
                binding.edtName.setText(this)
            }

            with(it.selectedDays) {
                updateSelectedDaysChips(this)
            }


        }
    }

    /**
     * Atualiza o estado de seleção (marcado/desmarcado) dos chips no `chipGroup` com base na lista fornecida de objetos `WeekDay`
     * obtida indiretamente atraves da observação do estado da ui no viewmodel
     *
     * Esta função percorre cada chip no `chipGroup` e determina se ele deve ser marcado, verificando se o número do dia associado a ele está presente na lista de `days` fornecida.
     *
     * @param days Uma lista de objetos `WeekDay` representando os dias que devem ser selecionados (marcados).
     */
    private fun updateSelectedDaysChips(days: List<WeekDay>) = with(binding) {
        // TODO: tentar otimizar
        val numberDays = days.map { it.dayNumber }

        // uso a tag que defini em cada chip pra associar o objeto [WeekDay] com a view correspondente
        chipGroup.children.toList().forEach { chip ->
            (chip as Chip).isChecked = numberDays.contains(chip.tag!!.toString().toInt())
        }
    }

    /**
     * Gerencia dinamicamente as views de TimeRange na UI, garantindo transições visuais suaves.
     *
     * A função mantém a interface sincronizada com a lista de TimeRanges fornecida,
     * removendo views obsoletas e adicionando novas conforme necessário.
     *
     * **Comportamento:**
     * - **Remoção de Views Obsoletas:** Remove views cujo TimeRange correspondente não existe mais no mapa.
     * - **Adição de Novas Views:** Adiciona novas views para TimeRanges que ainda não possuem uma representação visual.
     *
     * As novas views são criadas com data binding e adicionadas ao layout com uma animação suave.
     *
     * Obs: Tentei usar um RecyclerView para lidar com o dinamismo das views de TimeRanges, mas ele não anima bem quando seu tamanho não é fixo.
     * Afim de favorecer a estética do app por meio de animações agradaveis, retornei a essa abordagem manual para lidar com as views.
     *
     * @param timeRanges Um mapa de TimeRanges, onde a chave é o ID do intervalo e o valor é o objeto TimeRange.
     */
    private fun manageIntervalViews(timeRanges: Map<String, TimeRange>) {

        val parent = binding.llConteinerRanges

        parent.children
            .filter { it.tag !in timeRanges.keys }
            .forEach { parent.removeView(it) }

        timeRanges.values
            .filter { range -> parent.children.none { it.tag == range.id } }
            .forEach { range ->
                with(ItemIntervalBinding.inflate(layoutInflater)) {
                    tvStart.text = range.startIntervalFormatted()
                    tvEnd.text = range.endIntervalFormatted()
                    ivRemove.setOnClickListener(AnimatedClickListener {
                        vibrator.interaction()
                        viewModel.removeTimeRange(range)
                    })
                    root.tag = range.id
                    parent.addViewWithTwoStepsAnimation(root)
                }
            }
    }

}
