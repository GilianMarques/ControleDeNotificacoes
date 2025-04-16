package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentAddManagedAppsBinding
import dev.gmarques.controledenotificacoes.presentation.ui.ManagedAppsSharedViewModel
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import kotlin.getValue

@AndroidEntryPoint
class AddManagedAppsFragment : MyFragment() {


    companion object {
        fun newInstance(): AddManagedAppsFragment {
            return AddManagedAppsFragment()
        }
    }


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


}