package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentHomeBinding
import dev.gmarques.controledenotificacoes.databinding.ViewWarningBatteryOptimizationsBinding
import dev.gmarques.controledenotificacoes.databinding.ViewWarningListenNotificationPermissionBinding
import dev.gmarques.controledenotificacoes.databinding.ViewWarningPostNotificationsPermissionBinding
import dev.gmarques.controledenotificacoes.domain.Preferences
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.SlideTransition
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.addViewWithTwoStepsAnimation
import kotlinx.coroutines.delay
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
    lateinit var getInstalledAppIconUseCase: GetInstalledAppIconUseCase

    @Inject
    lateinit var getUserUseCase: GetUserUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        // Transição de entrada
        sharedElementEnterTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(SlideTransition())
            interpolator = AccelerateDecelerateInterpolator()
            duration = 420
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
    ): View = FragmentHomeBinding.inflate(inflater, container, false).also {
        binding = it
        setupUiWithUserData()
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            setupPopUpMenu()
            setupRecyclerView()
            observeViewModel()
            setupFabAddManagedApp()
            setupSearch()
        }
    }

    fun setupPopUpMenu() {
        val popupMenu = popupMenu {


            section {

                item {
                    label = getString(R.string.Atribuicoes)
                    icon = R.drawable.vec_add
                    callback = {
                        Toast.makeText(requireContext(), "implementar...", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

        binding.ivMenu.setOnClickListener(AnimatedClickListener {
            popupMenu.show(this@HomeFragment.requireContext(), binding.ivMenu)
        })
    }

    private fun setupUiWithUserData() = binding.apply {

        val user = getUserUseCase() ?: error("É necessário estar logado para chegar nesse ponto.")

        binding.tvUserName.text = user.name

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreetings.text = when (currentHour) {
            in 0..11 -> getString(R.string.Bom_dia)
            in 12..17 -> getString(R.string.Boa_tarde)
            else -> getString(R.string.Boa_noite)
        }

        user.photoUrl.let { photoUrl ->
            Glide.with(root.context).load(photoUrl).placeholder(R.drawable.ic_launcher_foreground)
                .transition(DrawableTransitionOptions.withCrossFade()).circleCrop().into(ivProfilePicture)
        }

        val views = listOf(ivProfilePicture, tvUserName, tvGreetings)

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

            binding.edtSearch.setText("")
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
            getInstalledAppIconUseCase,
            ::navigateToViewManagedAppFragment
        )

        rvApps.layoutManager = LinearLayoutManager(requireContext())
        rvApps.adapter = adapter
        rvApps.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    private fun navigateToViewManagedAppFragment(app: ManagedAppWithRule) {
        val extras = FragmentNavigatorExtras(
            binding.ivProfilePicture to "view_app_icon",
            binding.tvUserName to "view_app_name",
            binding.ivMenu to "view_menu",
            binding.divider to "divider",
            binding.fabAdd to "fab",
        )

        findNavController().navigate(
            HomeFragmentDirections.toViewManagedAppFragment(app = app), extras
        )
        binding.edtSearch.setText("")
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
            binding.edtSearch.isVisible = (apps?.size ?: 0) > 9

            lifecycleScope.launch {
                delay(300)
                binding.emptyView.parent.isGone = apps?.size != 0

            }
        }
    }

    private fun setupSearch() {
        binding.edtSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.managedAppsWithRules.value.let {
                it?.let { adapter.submitList(it, text.toString().trim()) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {

            binding.containerWarnings.removeAllViews()

            if (!requireMainActivity().isNotificationListenerEnabled()) {
                showListenNotificationWarning()
                return@launch
            }

            if (!requireMainActivity().isAppInsetFromBatterySaving()) {
                showBatteryRestrictionsWarning()
                return@launch
            }

            if (!requireMainActivity().isPostNotificationsPermissionEnable()) {
                lifecycleScope.launch {
                    if (readPreferenceUseCase(Preferences.SHOW_WARNING_CARD_POST_NOTIFICATION, true)) {
                        showPostNotificationRestrictionsWarning()
                    }
                    return@launch
                }
            }
        }
    }

    private fun showListenNotificationWarning() {

        val warningBinding = ViewWarningListenNotificationPermissionBinding.inflate(layoutInflater)

        warningBinding.chipPrivacy.setOnClickListener(AnimatedClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Sua_privacidade_importa))
                .setMessage(getString(R.string.Sua_privacidade_esta_protegida))
                .setPositiveButton(getString(R.string.Entendi)) { dialog, _ ->
                }.setIcon(R.drawable.vec_info).show()
        })

        warningBinding.chipGivePermission.setOnClickListener(AnimatedClickListener {
            requireMainActivity().requestNotificationAccessPermission()
            removerWarning(warningBinding.root)
        })

        binding.containerWarnings.addViewWithTwoStepsAnimation(warningBinding.root)
    }

    private fun showBatteryRestrictionsWarning() {

        val warningBinding = ViewWarningBatteryOptimizationsBinding.inflate(layoutInflater)

        warningBinding.chipGivePermission.setOnClickListener(AnimatedClickListener {
            requireMainActivity().requestIgnoreBatteryOptimizations()
            removerWarning(warningBinding.root)
        })

        binding.containerWarnings.addViewWithTwoStepsAnimation(warningBinding.root)
    }

    private fun showPostNotificationRestrictionsWarning() {

        val warningBinding = ViewWarningPostNotificationsPermissionBinding.inflate(layoutInflater)

        warningBinding.chipGivePermission.setOnClickListener(AnimatedClickListener {
            requireMainActivity().requestPostNotificationsPermission()
            removerWarning(warningBinding.root)
        })

        binding.containerWarnings.addViewWithTwoStepsAnimation(warningBinding.root)
    }

    private fun removerWarning(view: View) {
        lifecycleScope.launch {
            delay(1000)
            binding.containerWarnings.removeView(view)
        }
    }

}
