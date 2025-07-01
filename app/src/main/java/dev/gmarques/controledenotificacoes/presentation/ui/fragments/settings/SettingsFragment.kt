package dev.gmarques.controledenotificacoes.presentation.ui.fragments.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.transition.ChangeBounds
import androidx.transition.TransitionSet
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentSettingsBinding
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.SlideTransition

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 30 de maio de 2025 as 11:32.
 */
@AndroidEntryPoint
class SettingsFragment : MyFragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transitionInterpolator = android.view.animation.AccelerateDecelerateInterpolator()
        val transitionDuration = 400L

        sharedElementEnterTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(SlideTransition())
            interpolator = transitionInterpolator
            duration = transitionDuration
        }

        sharedElementReturnTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(SlideTransition())
            interpolator = transitionInterpolator
            duration = transitionDuration
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar(binding.actionbar)
        setupResetHintsButton()
        setupRequestNotificationAccessPermission()
        observeEvents()
        setupVersion()
    }


    private fun setupResetHintsButton() = with(binding) {
        tvResetHints.setOnClickListener(AnimatedClickListener {
            viewModel.resetHints()
        })
    }


    private fun setupRequestNotificationAccessPermission() = with(binding) {

        tvGiveReadNotificationPermission.setOnClickListener(AnimatedClickListener {
            requireMainActivity().requestNotificationAccessPermission()
        })

    }

    private fun setupVersion() {
        binding.tvVersion.text = BuildConfig.VERSION_NAME
    }

    private fun observeEvents() {
        collectFlow(viewModel.eventsFlow) { event ->
            when (event) {
                is SettingsEvent.PreferencesCleaned -> {
                    if (event.success) vibrator.success() else vibrator.error()

                    Snackbar.make(
                        binding.root, getString(
                            if (event.success) R.string.Os_di_logos_de_ajuda_ser_o_re_exibidos
                            else R.string.Houve_um_erro_tetando_resetar_as_prefer_ncias
                        ), Snackbar.LENGTH_LONG
                    ).show()
                }

                SettingsEvent.BatteryOptimizationWarningResetted -> {
                    vibrator.success()
                    Snackbar.make(
                        binding.root,
                        getString(R.string.O_aviso_de_otimiza_o_de_bateria_ser_reexibido_se_as_restri_es_estiverem_ativas),
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    }


}
