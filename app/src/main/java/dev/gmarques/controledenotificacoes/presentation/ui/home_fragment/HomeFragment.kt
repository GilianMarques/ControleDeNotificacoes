package dev.gmarques.controledenotificacoes.presentation.ui.home_fragment


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentHomeBinding
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment responsável por exibir a lista de aplicativos controlados.
 */
@AndroidEntryPoint
class HomeFragment : MyFragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: ManagedAppsAdapter

    @Inject
    lateinit var generateRuleNameUseCase: GenerateRuleNameUseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentHomeBinding
        .inflate(inflater, container, false)
        .also { binding = it }
        .root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupFabAddManagedApp()
    }

    private fun setupFabAddManagedApp() = with(binding) {
        fabAdd.setOnClickListener(AnimatedClickListener {
            findNavController().navigate(HomeFragmentDirections.toAddManagedAppsFragment())
        })
    }

    private fun setupRecyclerView() = with(binding) {

        adapter = ManagedAppsAdapter(
            getDrawable(R.drawable.vec_rule_permissive_small),
            getDrawable(R.drawable.vec_rule_restrictive_small),
            generateRuleNameUseCase::invoke
        )

        recyclerViewApps.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewApps.adapter = adapter
    }

    /**
     * Carrega os icones usados nas pelo recyclerview nas regras para indicar o tipo de regra (restritiva ou permissiva)
     */
    private fun getDrawable(id: Int): Drawable {
        return ResourcesCompat.getDrawable(resources, id, requireActivity().theme)
            ?: throw IllegalStateException("Drawable not found: $id")
    }

    /**
     * Observe a lista de ManagedAppWithRules no [HomeViewModel] e envia os dados para o [ManagedAppsAdapter]
     */
    private fun observeViewModel() = lifecycleScope.launch {
        collectFlow(viewModel.managedAppsWithRules) { apps ->
            adapter.submitList(apps)
        }
    }


}
