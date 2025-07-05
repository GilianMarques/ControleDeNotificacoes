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
import android.util.Log
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
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
    private var requestIgnoreBatteryOptimizationsJob: Job? = null

    private lateinit var appUpdateManager: AppUpdateManager

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Snackbar.make(
                binding.root,
                getString(R.string.Atualizacao_disponivel_para_instalacao),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.Instalar)) {
                appUpdateManager.completeUpdate()
            }.show()
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 22041961
        private const val UPDATE_REQUEST_CODE = 46251749
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
        checkForAppUpdate()
    }

    private fun checkForAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStateUpdatedListener)

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    @Suppress("DEPRECATION")
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (_: Exception) {

                }
            }
        }
    }

    override fun onStop() {
        requestIgnoreBatteryOptimizationsJob?.cancel()
        super.onStop()
    }

    private fun observeNavigationChanges() {

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { navController, destination, bundle ->

            if (destination.id != R.id.splashFragment &&
                destination.id != R.id.loginFragment
            ) applyDefaultBackgroundColor()

        }
    }

    /**
     * Os fragmentos são transparentes por isso preciso remover o background do splashscreen e definir uma cor sólida
     * na activity
     */
    private fun applyDefaultBackgroundColor() {

        if (backgroundChanged) return
        Log.d("USUK", "MainActivity.applyDefaultBackgroundColor: ")
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

                if (PreferencesImpl.showDialogNotPermissionDenied.isDefault()) {

                    MaterialAlertDialogBuilder(this@MainActivity).setTitle(getString(R.string.Permissao_nao_concedida))
                        .setMessage(getString(R.string.Voce_nao_ser_avisado_sobre_notifica_es_bloqueadas_ao_fim_do_per_odo_de_bloqueio_dos_apps_conceda_a_permiss_o_para_n_o_perder_alertas_importantes))
                        .setPositiveButton(getString(R.string.Entendi)) { _, _ ->
                            lifecycleScope.launch { PreferencesImpl.showDialogNotPermissionDenied(false) }
                        }.setCancelable(false).show()
                } else {
                    PreferencesImpl.showWarningCardPostNotification(false)
                }
            } else {
                App.instance.restartNotificationService()
            }

        }
    }

    fun isPostNotificationsPermissionEnable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
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
            this, getString(R.string.Permita_que_x_acesse_as_notificacoes, getString(R.string.app_name)), Toast.LENGTH_LONG
        ).show()
    }

    fun isAppInsetFromBatterySaving(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    /**
     * Tenta abrir a tela de otimização de bateria especifica do app e agenda a tela geral de otimização de bateria para ser aberta após um tempo
     * se a 1° tela for aberta o app sera minimizaodo e o [onStop] da activity vai cancelar o [requestIgnoreBatteryOptimizationsJob] que abriria a 2°
     * tela, senao a 2° tela será aberta para que o usuario procure o app manualmente e o isente das restrilções de bateria
     */
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations() {

        try {
            startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:$packageName".toUri()))
        } catch (_: Exception) {
        }

        requestIgnoreBatteryOptimizationsJob = lifecycleScope.launch {
            delay(1000)
            try {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (_: Exception) {
                showRequestIgnoreBatteryOptimizationErrorDialog()
            }
        }

    }

    private fun showRequestIgnoreBatteryOptimizationErrorDialog() {
        MaterialAlertDialogBuilder(this@MainActivity).setTitle(getString(R.string.Erro))
            .setMessage(getString(R.string.Nao_foi_poss_vel_abrir_a_tela_de_configuracoes))
            .setPositiveButton(R.string.Entendi) { _, _ -> }.setIcon(R.drawable.vec_alert).show()

    }

    override fun onDestroy() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
        super.onDestroy()
    }

    /**
     * Tenta abrir um app com base no id do pacote
     * @param packageId id do pacote do app a ser aberto ex: com.google.android.youtube
     * @return true se o app foi aberto com sucesso, false caso contrario
     */
    fun launchApp(packageId: String): Boolean {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageId)

        if (launchIntent != null) {
            startActivity(launchIntent)
            return true
        } else {
            return false
        }
    }
}



