package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.zawadz88.materialpopupmenu.popupMenu
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentSelectRuleBinding
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.dialogs.ConfirmRuleRemovalDialog
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import kotlinx.coroutines.launch

/**
 * Criado por Gilian Marques
 * Em sábado, 19 de abril de 2025 as 15:14.
 */
@AndroidEntryPoint
class SelectRuleFragment : MyFragment() {

    companion object {
        const val RESULT_LISTENER_KEY = "select_rule_listener_key"
        const val BUNDLED_RULE_KEY = "bundled_rule"
    }

    private lateinit var binding: FragmentSelectRuleBinding
    private val viewModel: SelectRuleViewModel by viewModels()

    private lateinit var adapter: RulesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentSelectRuleBinding.inflate(inflater, container, false).also {
            binding = it
            setupActionBar(binding.actionbar)
        }.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFabAddRule()
        observeViewModel()
    }

    private fun setupFabAddRule() = with(binding) {
        fabAdd.setOnClickListener(AnimatedClickListener {
            navigateToAddEditRuleFragment()
        })

    }

    private fun setupRecyclerView() {

        adapter = RulesAdapter(::rvOnRuleSelected, ::rvOnRuleEditClick)

        binding.rvApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SelectRuleFragment.adapter
            hideViewOnRVScroll(binding.rvApps, binding.fabAdd)
        }

    }

    private fun rvOnRuleSelected(rule: Rule) {
        val result = Bundle().apply {
            putSerializable(
                BUNDLED_RULE_KEY, rule
            )
        }

        setFragmentResult(RESULT_LISTENER_KEY, result)
        goBack()
    }

    private fun rvOnRuleEditClick(targetView: View, rule: Rule) {


        val popupMenu = popupMenu {

            section {

                item {
                    label = getString(R.string.Editar_regra)
                    icon = R.drawable.vec_edit
                    callback = {
                        navigateToAddEditRuleFragment(rule)
                    }
                }

                item {
                    label = getString(R.string.Remover_regra)
                    icon = R.drawable.vec_remove
                    callback = {
                        this@SelectRuleFragment.confirmRuleRemoval(rule)
                    }
                }

            }
        }
        popupMenu.show(requireContext(), targetView)


    }

    private fun confirmRuleRemoval(rule: Rule) {
        ConfirmRuleRemovalDialog(this@SelectRuleFragment, rule) {
            viewModel.deleteRule(it)
        }
    }

    private fun navigateToAddEditRuleFragment(rule: Rule? = null) {

        val extras = FragmentNavigatorExtras(
            binding.fabAdd to binding.fabAdd.transitionName
        )

        val destination = if (rule != null) SelectRuleFragmentDirections.toAddRuleFragment(rule)
        else SelectRuleFragmentDirections.toAddRuleFragment()

        findNavController().navigate(destination, extras)
    }

    private fun observeViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rules.collect { rules ->
                    binding.progressBar.isVisible = false
                    adapter.submitList(rules)
                }
            }
        }

        viewModel.ruleRemovalResult.observe(viewLifecycleOwner) { event ->

            val result = event?.consume()
            if (result?.isSuccess == true) vibrator.success()
            if (result?.isFailure == true) {
                showErrorSnackBar(getString(R.string.Hhouve_um_erro_ao_remover_a_regra_tente_novamente))
            }

        }

    }

}
