package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_condition

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentAddOrUpdateConditionBinding
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.domain.model.enums.ConditionType
import dev.gmarques.controledenotificacoes.domain.model.enums.NotificationField
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener


@AndroidEntryPoint
class AddOrUpdateConditionFragment : MyFragment() {

    companion object {
        const val RESULT_LISTENER_KEY = "add_update_condition_result"
        const val CONDITION_KEY = "condition"
    }

    private val viewModel: AddOrUpdateConditionViewModel by viewModels()
    private lateinit var binding: FragmentAddOrUpdateConditionBinding
    private val args: AddOrUpdateConditionFragmentArgs by navArgs()
    private var ruleTypeRestrictive = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentAddOrUpdateConditionBinding.inflate(inflater, container, false).also {
            binding = it
            setupActionBar(binding.toolbar)
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadArgs()
        setupConditionType()
        setupNotificationFields()
        setupKeywordsInput()
        setupCaseSensitive()
        setupFab()
        observeViewModel()
        observeEvents()
    }

    private fun loadArgs() {

        args.condition?.let {
            viewModel.setEditingCondition(it)
        }

        ruleTypeRestrictive = args.ruleTypeRestrictive
    }

    private fun setupConditionType() = with(binding) {
        mbtTypeCondition.addOnButtonCheckedListener { group: MaterialButtonToggleGroup, btnId: Int, checked: Boolean ->
            when (group.checkedButtonId) {
                R.id.btn_only_if -> viewModel.setConditionType(ConditionType.ONLY_IF)
                R.id.btn_except_if -> viewModel.setConditionType(ConditionType.EXCEPT)
            }
        }
    }

    private fun setupNotificationFields() = with(binding) {
        mbtField.addOnButtonCheckedListener { group: MaterialButtonToggleGroup, btnId: Int, checked: Boolean ->
            when (group.checkedButtonId) {
                R.id.btn_title -> viewModel.setField(NotificationField.TITLE)
                R.id.btn_content -> viewModel.setField(NotificationField.CONTENT)
                R.id.btn_both -> viewModel.setField(NotificationField.BOTH)
            }
        }
    }

    private fun setupKeywordsInput() = with(binding) {

        edtKeywords.hint = getString(R.string.Separe_os_termos_com_x, Condition.SEPARATOR)

        edtKeywords.doOnTextChanged { text, _, _, _ ->

            if (text?.contains(Condition.SEPARATOR) == true) {
                val keyword = text.split(Condition.SEPARATOR).first()
                if (keyword.isNotBlank()) viewModel.addKeyword(keyword)
                edtKeywords.setText("")
            }
        }

    }

    private fun setupCaseSensitive() = with(binding) {
        swCase.setOnCheckedChangeListener { _, checked ->
            viewModel.setCaseSensitive(checked)
        }
    }

    private fun setupFab() = with(binding) {

        fabAdd.setOnClickListener(AnimatedClickListener {
            viewModel.validateCondition()
        })
    }

    private fun observeViewModel() {

        collectFlow(viewModel.keywordsFlow) { keywords ->
            addKeywordsChips(keywords)
            updateConditionBehaviourHint()
        }

        collectFlow(viewModel.conditionTypeFlow) { type ->
            updateConditionType(type)
            updateConditionBehaviourHint()
        }

        collectFlow(viewModel.fieldFlow) { field ->
            updateNotificationField(field)
            updateConditionBehaviourHint()
        }

        collectFlow(viewModel.caseSensitiveFlow) { checked ->
            binding.swCase.isChecked = checked == true
            updateConditionBehaviourHint()
        }

        collectFlow(viewModel.conditionDone) { condition ->
            condition?.let {
                setFragmentResult(RESULT_LISTENER_KEY, Bundle().apply { putSerializable(CONDITION_KEY, condition) })
                goBack()
            }
        }
    }

    /**
     * Observa os estados da UI disparados pelo viewmodel chamando a função adequada para cada estado.
     * Utiliza a função collectFlow para coletar os estados do flow de forma segura e sem repetições de código.
     */
    private fun observeEvents() {
        collectFlow(viewModel.eventsFlow) { event ->
            when (event) {
                is Event.Error -> showErrorSnackBar(event.msg, binding.fabAdd)
            }
        }
    }

    private fun addKeywordsChips(keywords: List<String>) = with(binding) {

        chipGroupKeywords.removeAllViews()

        keywords.forEach { keyword ->

            with(Chip(requireContext())) {
                text = keyword
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    viewModel.removeKeyword(keyword)
                }
                chipGroupKeywords.addView(this)
            }
        }

    }

    private fun updateConditionType(type: ConditionType?) = with(binding) {

        tvInfoConditionType.isVisible = true

        when (type) {
            ConditionType.ONLY_IF -> {
                mbtTypeCondition.check(R.id.btn_only_if)
                tvInfoConditionType.text = getString(R.string.A_regra_ser_aplicada_apenas_se_a_condi_o_for_satisfeita)
            }

            ConditionType.EXCEPT -> {
                mbtTypeCondition.check(R.id.btn_except_if)
                tvInfoConditionType.text = getString(R.string.A_regra_NAO_ser_aplicada_se_a_condi_o_for_satisfeita)
            }

            null -> {

                mbtTypeCondition.check(0)
                tvInfoConditionType.isVisible = false
            }

        }
    }

    private fun updateNotificationField(field: NotificationField?) = with(binding) {

        tvFieldInfo.isVisible = true

        when (field) {

            NotificationField.TITLE -> {
                mbtField.check(R.id.btn_title)
                tvFieldInfo.text = getString(R.string.Buscar_palavras_chave_apenas_no_t_tulo_da_notifica_o)
            }

            NotificationField.CONTENT -> {
                mbtField.check(R.id.btn_content)
                tvFieldInfo.text = getString(R.string.Buscar_palavras_chave_apenas_no_conte_do_da_notifica_o)
            }

            NotificationField.BOTH -> {
                mbtField.check(R.id.btn_both)
                tvFieldInfo.text = getString(R.string.Buscar_palavras_chave_no_t_tulo_e_conte_do_da_notifica_o)
            }

            null -> {
                mbtField.check(0)
                tvFieldInfo.isVisible = false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateConditionBehaviourHint() {
// TODO: passar pro viewmodel
        val maxKeywords = 3
        var hint = ""

        hint += if (ruleTypeRestrictive) getString(R.string.Bloquear_notifica_es) else getString(R.string.Permitir_notifica_es)

        hint += when (viewModel.conditionTypeFlow.value) {
            ConditionType.ONLY_IF -> getString(R.string.apenas_se)
            ConditionType.EXCEPT -> getString(R.string.exceto_se)
            null -> {
                binding.tvSummary.text = "$hint..."
                return
            }
        }

        hint += when (viewModel.fieldFlow.value) {
            NotificationField.TITLE -> getString(R.string.o_t_tulo_contiver)
            NotificationField.CONTENT -> getString(R.string.o_conte_do_contiver)
            NotificationField.BOTH -> getString(R.string.o_t_tulo_ou_o_conte_do_contiverem)
            null -> {
                binding.tvSummary.text = "$hint..."
                return
            }
        }

        if (viewModel.keywordsFlow.value.size > maxKeywords) viewModel.keywordsFlow.value.forEachIndexed { index, keyword ->
            if (index >= maxKeywords) return@forEachIndexed
            hint += if (index + 1 < maxKeywords) " \"$keyword\","
            else " \"${keyword}\"..."
        } else {

            val maxKeywords = viewModel.keywordsFlow.value.size
            viewModel.keywordsFlow.value.forEachIndexed { index, keyword ->
                hint += if (index + 2 < maxKeywords) " \"$keyword\","
                else if (index + 1 < maxKeywords) " \"$keyword\""
                else if (maxKeywords > 1) getString(R.string.ou, keyword)
                else " \"$keyword\""
            }
        }

        hint += if (viewModel.keywordsFlow.value.isNotEmpty()) "," else " (*)"

        hint += if (viewModel.caseSensitiveFlow.value == true) getString(R.string.considerando_letras_mai_sculas_e_min_sculas)
        else getString(R.string.independentemente_de_letras_mai_sculas_e_min_sculas)

        binding.tvSummary.text = hint

    }


}