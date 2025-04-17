package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.databinding.FragmentAddManagedAppsBinding
import dev.gmarques.controledenotificacoes.databinding.ItemAppSmallBinding
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.select_apps_fragment.SelectAppsFragment
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
        setupSelectAppsListener()
        setupSelectAppsButton()
        setupSelectRuleButton()
        observeSharedViewModel()
    }

    private fun setupSelectAppsButton() = with(binding) {

        ivAddApp.setOnClickListener(AnimatedClickListener {
            vibrator.interaction()
            findNavController().navigate(AddManagedAppsFragmentDirections.toSelectAppsFragment(viewModel.getSelectedPackages()))
        })

    }

    @Suppress("UNCHECKED_CAST")
    private fun setupSelectAppsListener() {

        setFragmentResultListener(SelectAppsFragment.RESULT_KEY) { _, bundle ->
            val selectedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(
                    SelectAppsFragment.BUNDLED_SELECTED_APPS_KEY,
                    ArrayList::class.java
                ) as ArrayList<InstalledApp>
            } else {
                @Suppress("DEPRECATION")
                bundle.getSerializable(SelectAppsFragment.BUNDLED_SELECTED_APPS_KEY) as ArrayList<InstalledApp>
            }

            viewModel.setApps(selectedApps)
        }
    }

    private fun setupSelectRuleButton() = with(binding) {

        ivAddRule.setOnClickListener(AnimatedClickListener {
            vibrator.interaction()
            findNavController().navigate(AddManagedAppsFragmentDirections.toAddRuleFragment())
        })

    }

    private fun observeSharedViewModel() {

        viewModel.selectedApps.observe(viewLifecycleOwner) { apps ->
            manageAppsViews(apps)
        }

        viewModel.selectedRule.observe(viewLifecycleOwner) { rule ->
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
