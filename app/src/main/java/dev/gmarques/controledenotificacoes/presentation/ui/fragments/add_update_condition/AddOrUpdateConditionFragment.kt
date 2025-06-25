package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_condition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentAddOrUpdateConditionBinding
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment


@AndroidEntryPoint
class AddOrUpdateConditionFragment : MyFragment() {

    companion object {
        const val RESULT_LISTENER_KEY = "add_update_condition_result"
        const val CONDITION_KEY = "condition"
    }

    private val viewModel: AddOrUpdateConditionViewModel by viewModels()
    private lateinit var binding: FragmentAddOrUpdateConditionBinding
    private val args: AddOrUpdateConditionFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentAddOrUpdateConditionBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadArgs()
        setupKeywordsInputField()
        observeViewModel()
        observeEvents()
    }

    private fun observeViewModel() {
        collectFlow(viewModel.keywordsFlow) { keywords ->
            addKeywordsChips(keywords)
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

    private fun setupKeywordsInputField() = with(binding) {

        edtKeywords.hint = getString(R.string.Separe_os_termos_com_x, Condition.SEPARATOR)

        edtKeywords.doOnTextChanged { text, _, _, _ ->

            if (text?.contains(Condition.SEPARATOR) == true) {
                val keywords = text.split(Condition.SEPARATOR)
                viewModel.addKeywords(keywords)
                edtKeywords.setText("")
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

    private fun loadArgs() {

        args.condition?.let {
            viewModel.setEditingCondition(it)
        }

        viewModel.ruleTypeRestrictive = args.ruleTypeRestrictive

    }
}