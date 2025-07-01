package dev.gmarques.controledenotificacoes.presentation.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.databinding.FragmentProfileBinding
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.LogOffUserUseCase
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.SlideTransition
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : MyFragment() {

    @Inject
    lateinit var roomDatabase: RoomDatabase

    @Inject
    lateinit var logOffUserUseCase: LogOffUserUseCase

    @Inject
    lateinit var getUserUseCase: GetUserUseCase

    private lateinit var binding: FragmentProfileBinding

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transitionInterpolator = AccelerateDecelerateInterpolator()
        val enterExitDuration = 400L

        // Transição de entrada
        sharedElementEnterTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(SlideTransition())
            interpolator = transitionInterpolator
            duration = enterExitDuration
        }

        // Transição de retorno
        sharedElementReturnTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(SlideTransition())
            interpolator = transitionInterpolator
            duration = enterExitDuration
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar(binding.actionbar)
        loadUserData()
        setupLogOffButton()
        observeEvents()
    }

    /**
     * Observa os estados da UI disparados pelo viewmodel chamando a função adequada para cada estado.
     * Utiliza a função collectFlow para coletar os estados do flow de forma segura e sem repetições de código.
     */
    private fun observeEvents() {
        collectFlow(viewModel.eventsFlow) { event ->
            when (event) {
                is Event.PreferencesCleaned -> {

                    if (event.success) vibrator.success()
                    else vibrator.error()

                    Snackbar.make(
                        binding.nsv, getString(
                            if (event.success) R.string.Os_di_logos_de_ajuda_ser_o_re_exibidos
                            else R.string.Houve_um_erro_tetando_resetar_as_prefer_ncias
                        ),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setupLogOffButton() {
        binding.tvLogOff.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.Por_favor_confirme))
                .setMessage(getString(R.string.Voce_sera_desconectado_a_e_todos_os_dados_locais_ser_o_removidos_deseja_mesmo_continuar))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.Sair)) { dialog, _ ->
                    lifecycleScope.launch {
                        performLogOff()
                    }
                }.show()
        }
    }

    private suspend fun performLogOff() = withContext(IO) {

        logOffUserUseCase()
        if (!BuildConfig.DEBUG) roomDatabase.clearAllTables()
        vibrator.success()
        withContext(Main) { requireActivity().finish() }
    }

    private fun loadUserData() {
        val user = getUserUseCase() ?: error("usuario nao pode ser nulo aqui")

        binding.tvUserName.text = user.name

        user.photoUrl.let { photoUrl ->
            Glide.with(binding.root.context)
                .load(photoUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(binding.ivProfilePicture)
        }
    }
}
