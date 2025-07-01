package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule

import TimeRangeValidator.MAX_RANGES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.databinding.FragmentAddOrUpdateRuleBinding
import dev.gmarques.controledenotificacoes.databinding.ItemIntervalBinding
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.domain.model.ConditionExtensionFun.description
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_condition.AddOrUpdateConditionFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.addViewWithTwoStepsAnimation
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.setStartDrawable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

@AndroidEntryPoint
class AddOrUpdateRuleFragment : MyFragment() {

    private var doNotNotifyViewModelTypeRule: Boolean = true

    private val viewModel: AddOrUpdateRuleViewModel by viewModels()
    private val args: AddOrUpdateRuleFragmentArgs by navArgs()

    private lateinit var binding: FragmentAddOrUpdateRuleBinding

    companion object {
        const val RESULT_LISTENER_KEY = "add_update_rule_result"
        const val RULE_KEY = "rule"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentAddOrUpdateRuleBinding.inflate(inflater, container, false).also {
        binding = it
        setupActionBar(binding.actionbar)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            setupNameInput()
            setupButtonTypeRule()
            setupChipDays()
            setupChipDaysShortcuts()
            setupBtnAddTimeRange()
            setupBtnAddCondition()
            setupBtnRemoveCondition()
            setupFabAddRule()
            setupEditingModeIfNeeded()
            observeRuleType()
            observeTimeRanges()
            observeSelectedDays()
            observeRuleName()
            observeCondition()
            observeEvents()
        }
    }

    /**
     * Configura o modo de edição para a regra, caso uma regra para edição seja fornecida nos argumentos.
     *
     * Esta função verifica se `args.editingRule` não é nulo. Se não for nulo, isso significa que
     * o usuário pretende editar uma regra existente. Portanto, ela chama `viewModel.setEditingRule()`
     * com a regra fornecida. Essa ação informa ao ViewModel que estamos em modo de edição e
     * fornece a regra que precisa ser editada.
     *
     * Se `args.editingRule` for nulo, esta função não faz nada, implicando que não estamos em
     * modo de edição e que potencialmente estamos criando uma nova regra.
     *
     */
    private fun setupEditingModeIfNeeded() {
        args.editingRule?.let {
            viewModel.setEditingRule(it)
            binding.actionbar.tvTitle.text = getString(R.string.Editar_regra)

            showHintDialog(
                PreferencesImpl.showHintEditFirstRule,
                getString(R.string.Editar_uma_regra_faz_com_que_as_altera_es_feitas_se_apliquem_a_todos_os_aplicativos)
            )

        }
    }

    private fun setupFabAddRule() = with(binding) {
        fabAdd.setOnClickListener(AnimatedClickListener {
            edtName.clearFocus()
            lifecycleScope.launch {
                fabAdd.isClickable = false
                launch { viewModel.validateAndSaveRule() }
                delay(1500)
                fabAdd.isClickable = true
            }
        })
    }

    private fun setupNameInput() = with(binding) {
        edtName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.validateName(edtName.text.toString())
            }
        }
    }

    /**
     * Configura o comportamento dos chips dentro do chipGroupDays` para habilitar uma animação visual quando seu estado de seleção é alterado.
     *
     * Quando o estado de seleção de um chip é modificado, ele é brevemente removido e, em seguida, readicionado ao chipGroupDays` na sua posição original.
     * Esse processo dispara uma animação, fornecendo feedback visual ao usuário.
     *
     * Este méto.do itera sobre cada `Chip` do chipGroupDays` e associa um `setOnCheckedChangeListener` a ele. O listener executa as seguintes ações:
     *
     * 1. **Obter Índice Original:** Determina o índice atual do chip dentro do chipGroupDays`.
     * 2. **Remover Chip:** Remove temporariamente o chip do chipGroupDays`.
     * 3. **Readicionar Chip:** Adiciona o chip de volta ao chipGroupDays` no índice previamente determinado.
     *
     * Essa operação de remover e readicionar, no mesmo índice, força o layout a ser redesenhado, criando um efeito de animação visual sutil.
     *
     * Pressupostos:
     * - `binding` é um objeto `ViewBinding` válido.
     * - Todas as filhas do chipGroupDays` são instâncias de `Chip`.
     */
    private fun setupChipDays() = with(binding) {

        val animateChipCheck = { chip: View, index: Int ->
            chipGroupDays.removeView(chip)
            chipGroupDays.addView(chip, index)

        }

        val weekDayByNumber = WeekDay.entries.associateBy { it.dayNumber }

        for (view in chipGroupDays.children) {
            val chip = view as Chip

            chip.setOnCheckedChangeListener { buttonView, _ ->
                animateChipCheck(buttonView, chipGroupDays.indexOfChild(buttonView))

                val selectedDays = chipGroupDays.children.filter { (it as Chip).isChecked }
                    .map {
                        val dayNum = it.tag.toString().toInt()
                        weekDayByNumber[dayNum]!!
                    }.toList()

                viewModel.updateSelectedDays(selectedDays)

                viewModel.validateDays(selectedDays)
            }
        }

        edtName.clearFocus()
    }

    private fun setupChipDaysShortcuts() = with(binding) {

        fun toggleChips(range: List<Int>, check: Boolean) {

            chipGroupDays.forEach {
                val chip = it as Chip
                val tagValue = it.tag.toString().toIntOrNull()
                if (tagValue in range) {
                    chip.isChecked = check
                } else chip.isChecked = false
            }
        }


        chipWeekdays.setOnCheckedChangeListener { buttonView, isChecked ->
            toggleChips((2..6).toList(), isChecked)
        }

        chipWeekend.setOnCheckedChangeListener { buttonView, isChecked ->
            toggleChips(listOf(1, 7), isChecked)
        }

        chipEveryDay.setOnCheckedChangeListener { buttonView, isChecked ->
            toggleChips((1..7).toList(), isChecked)
        }

    }

    private fun setupButtonTypeRule() = with(binding) {
        mbtTypeRule.addOnButtonCheckedListener { group: MaterialButtonToggleGroup, btnId: Int, checked: Boolean ->

            if (doNotNotifyViewModelTypeRule) {
                doNotNotifyViewModelTypeRule = false
                return@addOnButtonCheckedListener
            }

            when (group.checkedButtonId) {
                R.id.btn_permissive -> viewModel.updateRuleType(RuleType.PERMISSIVE)
                R.id.btn_restritive -> viewModel.updateRuleType(RuleType.RESTRICTIVE)
            }
            edtName.clearFocus()
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
     */
    private fun setupBtnAddTimeRange() = with(binding) {
        tvAddRange.setOnClickListener(AnimatedClickListener {

            if (viewModel.canAddMoreRanges()) {
                showTimeRangeDialog()
            } else {
                showErrorSnackBar(
                    getString(
                        R.string.O_limite_m_ximo_de_intervalos_de_tempo_foi_atingido, MAX_RANGES
                    )
                )
            }
            edtName.clearFocus()
        })
    }

    private fun setupBtnRemoveCondition() = with(binding) {
        ivRemoveCondition.setOnClickListener(AnimatedClickListener {
            viewModel.removeCondition()
        })
    }

    private fun setupBtnAddCondition() = with(binding) {

        setFragmentResultListener(AddOrUpdateConditionFragment.RESULT_LISTENER_KEY) { _, bundle ->
            val condition = requireSerializableOf(bundle, AddOrUpdateConditionFragment.CONDITION_KEY, Condition::class.java)
                ?: error("nao pode ser nulo")
            viewModel.setCondition(condition)
        }


        tvAddCondition.setOnClickListener(AnimatedClickListener {

            val transitionExtras = FragmentNavigatorExtras(
                binding.fabAdd to "fab",
                binding.llRuleNameParent to "ll_condition_type_parent",
                binding.llRuleTypeParent to "ll_field_parent",
                binding.llWeekdaysParent to "ll_ignore_case",
                binding.llTimerangeParent to "ll_keywords_parent",
                binding.llConditionParent to "tv_summary",
            )

            findNavController().navigate(
                AddOrUpdateRuleFragmentDirections.toAddOrUpdateCondition(
                    viewModel.conditionFlow.value,
                    viewModel.ruleType.value == RuleType.RESTRICTIVE
                ), transitionExtras
            )
        })
    }

    private fun showTimeRangeDialog() {
        TimeRangeDialogManager(
            context = requireContext(),
            inflater = layoutInflater,
        ) { timeRange ->
            viewModel.validateRangesWithSequenceAndAdd(timeRange)
        }.show()
    }

    /**
     * Atualiza, com base nos updates do viewmodel a interface com base no tipo de regra (Permissiva ou Restritiva) .
     *
     * Modifica o estado do [MaterialButtonToggleGroup] e do TextView de acordo com [ruleType].
     *
     * @param ruleType O tipo de regra a ser aplicada ([RuleType.PERMISSIVE] ou [RuleType.RESTRICTIVE]).
     *
     * @see RuleType
     */
    private fun updateButtonTypeRule(ruleType: RuleType) = with(binding) {

        if (ruleType == RuleType.PERMISSIVE) {
            mbtTypeRule.check(R.id.btn_permissive)
            tvRuleTypeInfo.text = getString(R.string.Permite_mostrar_as_notifica_es_nos_dias_e_horarios_selecionados)

        } else {
            mbtTypeRule.check(R.id.btn_restritive)
            tvRuleTypeInfo.text = getString(R.string.As_notifica_es_ser_o_bloqueadas_nos_dias_e_hor_rios_selecionados)
        }

    }

    /**
     * Atualiza o estado de seleção dos chips no chipGroupDays` com base na lista de `WeekDay`.
     *
     * Cada chip é identificado pelo número do dia (0 a 6) definido em sua tag.
     *
     * @param days Lista de objetos `WeekDay` representando os dias selecionados.
     */
    private fun updateSelectedDaysChips(days: List<WeekDay>) = with(binding) {
        val numberDaysSet = days.map { it.dayNumber }.toSet()

        chipGroupDays.children.forEach { chip ->
            val dayNumber = chip.tag.toString().toInt()
            (chip as Chip).isChecked = dayNumber in numberDaysSet
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
    private fun manageTimeRangesViews(timeRanges: Map<String, TimeRange>) {

        val parent = binding.llContainerRanges
        val sortedRanges = timeRanges.values.toList().sortedBy { it.startHour }

        /* remova o `toList()` e veja sua vida se transformar em um inferno! Brincadeiras a parte, deve-se criar
                 uma lista de views a remover primeiro e só depois remova-las pra evitar inconsistencias na ui */
        parent.children.filter { it.tag !in timeRanges.keys }.toList().forEach { parent.removeView(it) }

        sortedRanges.forEachIndexed { index, range ->
            if (!parent.children.none { it.tag == range.id }) return@forEachIndexed

            with(ItemIntervalBinding.inflate(layoutInflater)) {

                if (range.allDay) tv24h.isVisible = true
                else {
                    tvStart.text = range.startIntervalFormatted()
                    tvEnd.text = range.endIntervalFormatted()
                }
                ivRemove.setOnClickListener(AnimatedClickListener {

                    viewModel.deleteTimeRange(range)
                })
                root.tag = range.id
                parent.addViewWithTwoStepsAnimation(root, min(index, parent.childCount))
            }
        }
    }

    private fun observeRuleType() {
        collectFlow(viewModel.ruleType) { type ->
            doNotNotifyViewModelTypeRule = true
            updateButtonTypeRule(type)
        }
    }

    private fun observeTimeRanges() {
        collectFlow(viewModel.timeRanges) { ranges ->
            manageTimeRangesViews(ranges)
        }
    }

    private fun observeSelectedDays() {
        collectFlow(viewModel.selectedDays) { days ->
            updateSelectedDaysChips(days)
        }
    }

    private fun observeRuleName() {
        collectFlow(viewModel.ruleName) { name ->
            binding.edtName.setText(name)
        }
    }

    private fun observeCondition() {
        collectFlow(viewModel.conditionFlow) { condition ->

            if (condition == null) {
                binding.llContainerConditions.isGone = true
                binding.ivRemoveCondition.isGone = true
                binding.tvAddCondition.text = getString(R.string.Adicionar)
                binding.tvAddCondition.setStartDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.vec_add)!!)
                return@collectFlow
            }

            binding.ivRemoveCondition.isVisible = true
            binding.llContainerConditions.isVisible = true
            binding.tvAddCondition.text = getString(R.string.Editar)
            binding.tvAddCondition.setStartDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.vec_edit_small)!!)

            with(binding) {
                tvName.text = condition.description(
                    requireContext()
                )
            }
        }
    }

    /**
     * Observa os eventos emitidos pelo ViewModel através de `eventsFlow` e os trata.
     *
     * Esta função coleta eventos assincronamente do `eventsFlow` do ViewModel e executa ações na UI
     * com base no tipo de evento recebido.
     *
     * **Eventos Tratados:**
     * - `Event.NameErrorMessage`: Define a mensagem de erro no campo de texto do nome da regra (`edtName`)
     *   e exibe uma SnackBar com a mesma mensagem.
     * - `Event.SetResultAndClose`: Define o resultado do Fragmento com a regra fornecida e navega de volta.
     * - `Event.SimpleErrorMessage`: Exibe uma SnackBar com uma mensagem de erro simples.
     *
     * @see showErrorSnackBar
     * @see vibrator
     * @see goBack
     */
    private fun observeEvents() {
        collectFlow(viewModel.eventsFlow) { event ->
            when (event) {

                is Event.NameErrorMessage -> {
                    binding.edtName.error = event.data
                    showErrorSnackBar(event.data, binding.fabAdd)
                }

                is Event.SetResultAndClose -> {
                    setResultAndClose(event.data)
                }

                is Event.SimpleErrorMessage -> {
                    showErrorSnackBar(event.data, binding.fabAdd)
                }
            }

        }

    }

    private fun setResultAndClose(rule: Rule) {
        val bundle = Bundle().apply { putSerializable(RULE_KEY, rule) }
        setFragmentResult(RESULT_LISTENER_KEY, bundle)
        vibrator.success()
        goBack()
    }

}


