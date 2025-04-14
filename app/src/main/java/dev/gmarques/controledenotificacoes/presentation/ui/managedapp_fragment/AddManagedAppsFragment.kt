package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.databinding.FragmentAddManagedAppsBinding
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener

@AndroidEntryPoint
class AddManagedAppsFragment : Fragment() {


    companion object {
        fun newInstance(): AddManagedAppsFragment {
            return AddManagedAppsFragment()
        }
    }


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
        setupAddAppButton()
        showSelectAppsDialog()
    }


    private fun setupAddAppButton() {

        binding.ivAddApp.setOnClickListener(AnimatedClickListener {
            showSelectAppsDialog()
        })

        binding.ivAddApp.performClick()
    }


    private fun showSelectAppsDialog() {

        val bottomSheet = AppListBottomSheet.newInstance(
            viewModel.getInstalledAppsUseCase
        ) { selectedApp, checked ->
            viewModel.updateSelectedApps(selectedApp, checked)
        }

        bottomSheet.show(parentFragmentManager, "AppListBottomSheet")
    }
}