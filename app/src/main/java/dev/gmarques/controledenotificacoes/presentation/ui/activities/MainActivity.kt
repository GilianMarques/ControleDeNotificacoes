package dev.gmarques.controledenotificacoes.presentation.ui.activities

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.ActivityMainBinding


/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var splashLabel: String
    private var backgroundChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        splashLabel = getString(R.string.Splash_fragment)

        enableEdgeToEdge()

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        observeNavigationChanges()

        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun observeNavigationChanges() {

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener(::applyDefaultBackgroundColor)
    }

    /**
     * Os fragmentos são transparentes por isso preciso remover o background do splashscreen e definir uma cor sólida
     * na activity
     */
    @Suppress("unused")
    private fun applyDefaultBackgroundColor(navController: NavController, destination: NavDestination, bundle: Bundle?) {

        if (backgroundChanged) return

        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorBackground, typedValue, true)
        window.decorView.setBackgroundColor(typedValue.data)

        backgroundChanged = true
    }
}