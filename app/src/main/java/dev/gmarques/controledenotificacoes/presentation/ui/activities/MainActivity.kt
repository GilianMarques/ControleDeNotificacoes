package dev.gmarques.controledenotificacoes.presentation.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.ActivityMainBinding
import dev.gmarques.controledenotificacoes.domain.Preferences
import dev.gmarques.controledenotificacoes.domain.Preferences.SHOW_WARNING_CARD_POST_NOTIFICATION
import dev.gmarques.controledenotificacoes.domain.usecase.settings.ReadPreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.settings.SavePreferenceUseCase
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationServiceManager
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var backgroundChanged = false
    private lateinit var splashLabel: String
    private lateinit var homeLabel: String
    private var currentFragmentLabel = ""

    @Inject
    lateinit var readPreferenceUseCase: ReadPreferenceUseCase

    @Inject
    lateinit var savePreferenceUseCase: SavePreferenceUseCase

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 22041961
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        splashLabel = getString(R.string.Splash_fragment)
        homeLabel = getString(R.string.Fragment_home)

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
        startNotificationListenerServiceManager()
    }

    private fun observeNavigationChanges() {

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { navController, destination, bundle ->
            currentFragmentLabel = destination.label.toString()

            if (currentFragmentLabel != getString(R.string.Splash_fragment)) applyDefaultBackgroundColor()

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (!granted) {

                if (readPreferenceUseCase(Preferences.SHOW_DIALOG_NOT_PERMISSION_DENIED, true)) {

                    MaterialAlertDialogBuilder(this@MainActivity).setTitle(getString(R.string.Permissao_nao_concedida))
                        .setMessage(getString(R.string.Voce_nao_ser_avisado_sobre_notifica_es_bloqueadas_ao_fim_do_per_odo_de_bloqueio_dos_apps_conceda_a_permiss_o_para_n_o_perder_alertas_importantes))
                        .setPositiveButton(getString(R.string.Entendi)) { _, _ ->
                            lifecycleScope.launch { savePreferenceUseCase(Preferences.SHOW_DIALOG_NOT_PERMISSION_DENIED, false) }
                        }.setCancelable(false).show()
                } else {
                    savePreferenceUseCase(SHOW_WARNING_CARD_POST_NOTIFICATION, false)
                }
            } else {
                restartNotificationListenerServiceManager()
            }

        }
    }

    fun isPostNotificationsPermissionEnable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun isNotificationListenerEnabled(): Boolean {

        val enabledListeners = Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        ) ?: return false

        return enabledListeners.split(":").any { it.contains(packageName) }
    }

    fun requestNotificationAccessPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
        Toast.makeText(
            this,
            getString(R.string.Permita_que_x_acesse_as_notificacoes, getString(R.string.app_name)),
            Toast.LENGTH_LONG
        ).show()
    }

    fun isAppInsetFromBatterySaving(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    fun requestIgnoreBatteryOptimizations() {

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            @SuppressLint("BatteryLife")
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:$packageName".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }

    fun startNotificationListenerServiceManager() {
        ContextCompat.startForegroundService(this, Intent(this, NotificationServiceManager::class.java))
    }

    fun restartNotificationListenerServiceManager() {

        val intent = Intent(this, NotificationServiceManager::class.java)
        stopService(intent)

        startNotificationListenerServiceManager()

    }

}



