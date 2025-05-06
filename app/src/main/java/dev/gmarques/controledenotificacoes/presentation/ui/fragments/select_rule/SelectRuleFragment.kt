package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import androidx.recyclerview.widget.RecyclerView
import com.github.zawadz88.materialpopupmenu.popupMenu
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentSelectRuleBinding
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.dialogs.ConfirmRuleRemovalDialog
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    @Inject
    lateinit var generateRuleNameUseCase: GenerateRuleNameUseCase

    private var animatingFab = false
    private var isFabVisible = true

    private lateinit var adapter: RulesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentSelectRuleBinding.inflate(inflater, container, false).also {
            binding = it
            setupActionBar(binding.toolbar)
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

        adapter = RulesAdapter(
            generateRuleNameUseCase, ::rvOnRuleSelected, ::rvOnRuleEditClick
        )

        binding.rvApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SelectRuleFragment.adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && isFabVisible) {
                        isFabVisible = false
                        toggleFabVisibility(false)
                    } else if (dy < 0 && !isFabVisible) {
                        isFabVisible = true
                        toggleFabVisibility(true)
                    }
                }
            })

        }

    }

    /**
     * Alterna a visibilidade do Floating Action Button (FAB) com uma animação de transição.
     *
     * Se o FAB já estiver em processo de animação (`animatingFab` é `true`), a função retorna
     * imediatamente para evitar animações concorrentes.  Caso contrário, define a translação Y
     * do FAB para mostrar ou esconder o botão e inicia a animação.
     *
     * @param show `true` para mostrar o FAB, `false` para escondê-lo.
     */
    private fun toggleFabVisibility(show: Boolean) = with(binding) {

        if (animatingFab) return@with

        val translationY = if (show) 0f else (fabAdd.height * 2f)

        fabAdd.animate().translationY(translationY).setDuration(400L)
            .setInterpolator(android.view.animation.AnticipateOvershootInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    animatingFab = true
                    super.onAnimationStart(animation)
                }

                override fun onAnimationEnd(animation: Animator) {
                    animatingFab = false
                    super.onAnimationEnd(animation)
                }
            }).start()
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
                    icon = R.drawable.vec_edit_rule
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
