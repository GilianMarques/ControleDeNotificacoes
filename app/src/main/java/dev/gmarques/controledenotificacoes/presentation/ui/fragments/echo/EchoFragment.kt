package dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dev.gmarques.controledenotificacoes.databinding.FragmentEchoBinding
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.setup_flow.EchoFlowSharedViewModel

/**
 *Criado por Gilian Marques
 * Em 12/06/2025 as 14:29
 */
class EchoFragment : MyFragment() {

    private val viewModel: EchoFlowSharedViewModel by viewModels()
    private lateinit var binding: FragmentEchoBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentEchoBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}
