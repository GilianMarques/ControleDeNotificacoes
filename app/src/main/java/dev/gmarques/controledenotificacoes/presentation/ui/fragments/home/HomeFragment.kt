package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home


import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentHomeBinding
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.SlideTransition
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
    lateinit var generateRuleNameUseCase: GenerateRuleNameUseCase

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
    ): View = FragmentHomeBinding.inflate(inflater, container, false).also {
        binding = it
        setupUiWithUserData()
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            setupRecyclerView()
            observeViewModel()
            setupFabAddManagedApp()
            setupSearch()
        }
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
            Glide.with(root.context)
                .load(photoUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(ivProfilePicture)
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
            generateRuleNameUseCase::invoke,
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
        )

        findNavController().navigate(
            HomeFragmentDirections.toViewManagedAppFragment(app), extras
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
        }
    }

    private fun setupSearch() {
        binding.edtSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.managedAppsWithRules.value.let {
                it?.let { adapter.submitList(it, text.toString()) }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isNotificationListenerEnabled())
            lifecycleScope.launch { delay(1500); showNotificationListenerPermissionDialog() }
    }

    private fun isNotificationListenerEnabled(): Boolean {

        val enabledListeners = Settings.Secure.getString(
            requireActivity().contentResolver, "enabled_notification_listeners"
        ) ?: return false

        return enabledListeners.split(":").any { it.contains(requireActivity().packageName) }
    }

    private fun showNotificationListenerPermissionDialog() {


        MaterialAlertDialogBuilder(requireActivity()).setTitle(getString(R.string.Permissao_necessaria))
            .setMessage(getString(R.string.Para_que_esse_aplicativo_possa_desempenhar_sua_fun_o_necess_rio_que_voc_forne_a_permiss_o_para_acesso_))
            .setPositiveButton(
                getString(R.string.Permitir), object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        startActivity(intent)
                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.Permita_que_x_acesse_as_notificacoes, getString(R.string.app_name)),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }).setNegativeButton(
                getString(R.string.Sair), object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        requireActivity().finish()
                    }
                }).setCancelable(false).setIcon(R.drawable.vec_permission).show()


    }


}
