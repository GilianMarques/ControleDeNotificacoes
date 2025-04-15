package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.databinding.FragmentAddManagedAppsBinding
import dev.gmarques.controledenotificacoes.domain.repository.AppRepository
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddManagedAppsFragment : Fragment() {

    companion object {
        fun newInstance(): AddManagedAppsFragment {
            return AddManagedAppsFragment()
        }
    }

    @Inject
    lateinit var appRepository: AppRepository //TODO  criar um usecase

    private val viewModel: AddManagedAppsFragementViewModel by viewModels()
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
        setupAddAppButton()
    }

    private fun setupAddAppButton() {

        binding.ivAddApp.setOnClickListener(AnimatedClickListener {
            lifecycleScope.launch {

                val apps = appRepository.getInstalledApps()

                val bottomSheet = AppListBottomSheet.newInstance(apps) { appSelecionado ->
                    // Faça algo com o app selecionado
                }

                bottomSheet.show(parentFragmentManager, "AppListBottomSheet")
            }
        })

        binding.ivAddApp.performClick()
    }
}