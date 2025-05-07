package dev.gmarques.controledenotificacoes.presentation.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.ActivityMainBinding
import dev.gmarques.controledenotificacoes.framework.notification_service.NotificationServiceManager


/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var backgroundChanged = false
    private lateinit var splashLabel: String
    private var currentFragmentLabel = ""

    @SuppressLint("SourceLockedOrientationActivity")
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


        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        }

        observeNavigationChanges()

        ContextCompat.startForegroundService(this, Intent(this, NotificationServiceManager::class.java))


    }

    private fun observeNavigationChanges() {

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { navController, destination, bundle ->
            applyDefaultBackgroundColor()
            currentFragmentLabel = destination.label.toString()
        }
    }

    /**
     * Os fragmentos são transparentes por isso preciso remover o background do splashscreen e definir uma cor sólida
     * na activity
     */
    private fun applyDefaultBackgroundColor() {

        if (backgroundChanged) return

        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.AppColorBackground, typedValue, true)
        window.decorView.setBackgroundColor(typedValue.data)

        backgroundChanged = true
    }

    /*
        private fun isAppInsetFromBatterySaving(): Boolean {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            return pm.isIgnoringBatteryOptimizations(packageName)
        }

        fun requestIgnoreBatteryOptimizations() {

            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
        }*/
}