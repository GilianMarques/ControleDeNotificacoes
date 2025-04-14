package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentAddManagedAppsBinding
import dev.gmarques.controledenotificacoes.databinding.ItemAppSmallBinding
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.ui.ManagedAppsSharedViewModel
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.addViewWithTwoStepsAnimation
import javax.inject.Inject

@AndroidEntryPoint
class AddManagedAppsFragment : MyFragment() {


    companion object {
        fun newInstance(): AddManagedAppsFragment {
            return AddManagedAppsFragment()
        }
    }

    @Inject
    lateinit var generateRuleNameUseCase: GenerateRuleNameUseCase

    private val viewModel: AddManagedAppsFragmentViewModel by viewModels()
    private val sharedViewModel: ManagedAppsSharedViewModel by navGraphViewModels(R.id.nav_graph_manage_apps_xml)

    private lateinit var binding: FragmentAddManagedAppsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddManagedAppsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActionBar(binding.toolbar)
        setupAddAppButton()
        setupAddRuleButton()
        observeSharedViewModel()
    }


    private fun setupAddAppButton() = with(binding) {

        ivAddApp.setOnClickListener(AnimatedClickListener {
            findNavController().navigate(AddManagedAppsFragmentDirections.toSelectAppsFragment())
        })

    }

    private fun setupAddRuleButton() = with(binding) {

        ivAddRule.setOnClickListener(AnimatedClickListener {
            findNavController().navigate(AddManagedAppsFragmentDirections.toAddRuleFragment())
        })

    }

    private fun observeSharedViewModel() {

        sharedViewModel.selectedApps.observe(viewLifecycleOwner) { apps ->
            manageAppsViews(apps)
        }

        sharedViewModel.selectedRule.observe(viewLifecycleOwner) { rule ->
            rule?.let { manageRuleView(rule) }
        }

    }

    private fun manageAppsViews(apps: Map<String, InstalledApp>) {
        val parent = binding.llConteinerApps

        parent.children
            .filter { it.tag !in apps.keys }
            .forEach { parent.removeView(it) }

        apps.values
            .forEach { app ->
                if (!parent.children.none { it.tag == app.packageId }) return@forEach

                with(ItemAppSmallBinding.inflate(layoutInflater)) {
                    name.text = app.name
                    ivAppIcon.setImageDrawable(app.icon)
                    root.tag = app.packageId
                    parent.addViewWithTwoStepsAnimation(root)
                }
            }
    }

    private fun manageRuleView(rule: Rule) = with(binding) {

        with(rule.name) {
            tvSelectedRule.text = if (this.isEmpty()) generateRuleNameUseCase(rule) else this
        }

    }
}
