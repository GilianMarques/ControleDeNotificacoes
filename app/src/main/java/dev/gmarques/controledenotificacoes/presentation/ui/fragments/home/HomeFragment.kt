package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isGone
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentHomeBinding
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.SlideTransition
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Fragment responsável por exibir a lista de aplicativos controlados.
 */
@AndroidEntryPoint
class HomeFragment : MyFragment() {


    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: ManagedAppsAdapter

    @Inject
    lateinit var generateRuleNameUseCase: GenerateRuleNameUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        // Transição de entrada
        sharedElementEnterTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(SlideTransition())
            interpolator = AccelerateDecelerateInterpolator()
            duration = 350
        }

        // Transição de retorno
        sharedElementReturnTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(SlideTransition())
            interpolator = AccelerateDecelerateInterpolator()
            duration = 350
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentHomeBinding
        .inflate(inflater, container, false)
        .also {
            binding = it
            setupUiWithUserData()
        }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupFabAddManagedApp()
        setupSearch()
    }

    private fun setupUiWithUserData() = binding.apply {

        val user = FirebaseAuth.getInstance().currentUser
            ?: error("É necessário estar logado para chegar nesse ponto.")

        binding.tvUserName.text = user.displayName

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreetings.text = when (currentHour) {
            in 0..11 -> getString(R.string.Bom_dia)
            in 12..17 -> getString(R.string.Boa_tarde)
            else -> getString(R.string.Boa_noite)
        }

        user.photoUrl?.let { photoUrl ->
            Glide.with(root.context).load(photoUrl).circleCrop().into(ivProfilePicture)
        } ?: run {
            // Esconde a imagem se não houver foto
            ivProfilePicture.visibility = View.GONE
            tvUserName.visibility = View.GONE
            tvGreetings.visibility = View.GONE
        }

        val views = listOf(ivProfilePicture, tvUserName, tvGreetings, ivMenu)

        views.forEach {
            it.setOnClickListener(AnimatedClickListener {
                val extras = FragmentNavigatorExtras(
                    tvUserName to tvUserName.transitionName,
                    ivProfilePicture to ivProfilePicture.transitionName,
                    divider to divider.transitionName,
                )
                findNavController().navigate(HomeFragmentDirections.toProfileFragment(), extras)
            })
        }
    }

    private fun setupFabAddManagedApp() = with(binding) {
        fabAdd.setOnClickListener(AnimatedClickListener {
            val extras = FragmentNavigatorExtras(
                binding.fabAdd to binding.fabAdd.transitionName
            )
            findNavController().navigate(HomeFragmentDirections.toAddManagedAppsFragment(), extras)
        })
    }

    private fun setupRecyclerView() = with(binding) {

        adapter = ManagedAppsAdapter(
            getDrawable(R.drawable.vec_rule_permissive_small),
            getDrawable(R.drawable.vec_rule_restrictive_small),
            generateRuleNameUseCase::invoke,
            ::navigateToViewManagedAppFragment
        )

        rvApps.layoutManager = LinearLayoutManager(requireContext())
        rvApps.adapter = adapter
        rvApps.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    private fun navigateToViewManagedAppFragment(app: ManagedAppWithRule, tvAppName: View, tvRuleName: View, ivIcon: View) {
        tvAppName.transitionName = "view_app_name"
        tvRuleName.transitionName = "view_rule_name"
        ivIcon.transitionName = "view_app_icon"

        val extras = FragmentNavigatorExtras(
            tvAppName to tvAppName.transitionName,
            tvRuleName to tvRuleName.transitionName,
            ivIcon to ivIcon.transitionName,
        )
        findNavController().navigate(
            HomeFragmentDirections.toViewManagedAppFragment(app),
            extras
        )
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
            binding.progressBar.isGone = apps != null
        }
    }

    private fun setupSearch() {
        binding.tietSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.managedAppsWithRules.value.let {
                it?.let { adapter.submitList(it, text.toString()) }
            }
        }
    }

}
