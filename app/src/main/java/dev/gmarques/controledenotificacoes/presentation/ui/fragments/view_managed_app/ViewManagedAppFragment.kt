package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.zawadz88.materialpopupmenu.popupMenu
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentViewManagedAppBinding
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.DomainRelatedExtFuns.getAdequateIconReferenceSmall
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.setRuleDrawable
import javax.inject.Inject

@AndroidEntryPoint
class FragmentViewManagedApp() : MyFragment() {

    companion object {
        fun newInstance(): FragmentViewManagedApp {
            return FragmentViewManagedApp()
        }
    }

    @Inject
    lateinit var generateRuleNameUseCase: GenerateRuleNameUseCase

    private val viewModel: ViewManagedAppViewModel by viewModels()
    private lateinit var binding: FragmentViewManagedAppBinding
    private val args: FragmentViewManagedAppArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentViewManagedAppBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setup(args.app)
        observeRuleChanges()
    }

    private fun setupActionBar(app: ManagedAppWithRule) = with(binding) {

        val drawable = ContextCompat.getDrawable(requireActivity(), app.rule.getAdequateIconReferenceSmall())
            ?: error("O icone deve existir pois é um recurso interno do app")

        tvAppName.text = app.name
        tvRuleName.text = app.rule.name.ifBlank { generateRuleNameUseCase(app.rule) }
        tvRuleName.setRuleDrawable(drawable)

        ivAppIcon.setImageDrawable(app.icon)

        ivGoBack.setOnClickListener(AnimatedClickListener {
            goBack()
        })

        ivMenu.setOnClickListener(AnimatedClickListener {
            showMenu()
        })

    }

    private fun showMenu() {
        val popupMenu = popupMenu {

            section {
                item {
                    label = getString(R.string.Limpar_historico)
                    icon = R.drawable.vec_try_again
                    callback = {
                        clearHistory()
                    }
                }
            }

            section {
                item {
                    label = getString(R.string.Remover_app)
                    icon = R.drawable.vec_remove
                    callback = {
                        confirmRemoveApp()
                    }
                }
            }

            section {
                title = getString(R.string.Regras)

                item {
                    label = getString(R.string.Editar_regra)
                    icon = R.drawable.vec_edit_rule
                    callback = {
                        navigateToEditRule()
                    }
                }

                item {
                    label = getString(R.string.Remover_regra)
                    icon = R.drawable.vec_remove
                    callback = {
                        confirmRemoveRule()
                    }
                }


            }

        }
        popupMenu.show(this@FragmentViewManagedApp.requireContext(), binding.ivMenu)
    }

    private fun clearHistory() {
        Toast.makeText(requireContext(), "implementar...", Toast.LENGTH_SHORT).show()
    }

    private fun confirmRemoveApp() {
        Toast.makeText(requireContext(), "implementar...", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToEditRule() {
        findNavController().navigate(FragmentViewManagedAppDirections.toAddRuleFragment(viewModel.managedAppFlow.value.rule))
    }

    private fun confirmRemoveRule() {
        Toast.makeText(requireContext(), "implementar...", Toast.LENGTH_SHORT).show()
    }

    private fun observeRuleChanges() {
        collectFlow(viewModel.managedAppFlow) { app ->
            setupActionBar(app)
        }
    }


}