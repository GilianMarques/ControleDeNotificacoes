package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.databinding.FragmentViewManagedAppBinding
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.DomainRelatedExtFuns.getAdequateIconReference
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
        viewModel.setApp(args.app)
        observeEvents()

    }


    private fun setupActionBar(app: ManagedAppWithRule) = with(binding) {

        val drawable = ContextCompat.getDrawable(requireActivity(), app.rule.getAdequateIconReference())
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

    }

    /**
     * Observa os estados da UI disparados pelo viewmodel chamando a função adequada para cada estado.
     * Utiliza a função collectFlow para coletar os estados do flow de forma segura e sem repetições de código.
     */
    private fun observeEvents() {
        collectFlow(viewModel.eventsFlow) { event ->
            when (event) {
                is ViewManagedAppsEvent.UpdateToolBar -> setupActionBar(event.app)
            }
        }
    }

}