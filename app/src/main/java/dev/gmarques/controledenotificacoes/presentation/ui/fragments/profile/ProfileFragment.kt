package dev.gmarques.controledenotificacoes.presentation.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.databinding.FragmentProfileBinding
import dev.gmarques.controledenotificacoes.domain.usecase.GetUserUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.LogOffUserUseCase
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.SlideTransition
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : MyFragment() {
    // TODO: otimizar isso pq foi feito as pressas
    @Inject
    lateinit var roomDatabase: RoomDatabase

    @Inject
    lateinit var logOffUserUseCase: LogOffUserUseCase

    @Inject
    lateinit var getUserUseCase: GetUserUseCase

    private lateinit var binding: FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Transição de entrada
        sharedElementEnterTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(SlideTransition())
            interpolator = AccelerateDecelerateInterpolator()
            duration = 450
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar(binding.toolbar)
        loadUserData()
        setupLogOff()
    }

    private fun setupLogOff() {
        binding.tvLogOff.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Por_favor_confirme))
                .setMessage(getString(R.string.Voce_sera_desconectado_a_e_todos_os_dados_locais_ser_o_removidos_deseja_mesmo_continuar))
                .setCancelable(true).setPositiveButton(getString(R.string.Sair)) { dialog, _ ->
                    lifecycleScope.launch {
                        makeLogOff()
                    }
                }.show()
        }
    }

    private suspend fun makeLogOff() = withContext(IO) {

        logOffUserUseCase()
        if (!BuildConfig.DEBUG) roomDatabase.clearAllTables()
        vibrator.success()
        requireActivity().finish()

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
