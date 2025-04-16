package dev.gmarques.controledenotificacoes.presentation.ui.home_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment

class HomeFragment : MyFragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    @Suppress("unused")
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().navigate(HomeFragmentDirections.toAddEditManagedAppsGraph())
    }
}