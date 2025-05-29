package dev.gmarques.controledenotificacoes

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.framework.model.RemoteConfigValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@HiltAndroidApp
class App() : Application(), CoroutineScope by MainScope() {

    companion object {
        lateinit var context: App
    }

    private val _remoteConfigValues = MutableStateFlow<RemoteConfigValues?>(null)
    val remoteConfigValues get() = _remoteConfigValues

    override fun onCreate() {
        context = this
        setupRemoteConfig()
        setupCrashLytics()
        super.onCreate()
    }

    private fun setupCrashLytics() {

        val getAppUserUseCase = HiltEntryPoints.getAppUserUseCase()

        FirebaseCrashlytics.getInstance().apply {
            setCustomKeys {
                key("environment", if (BuildConfig.DEBUG) "Debug" else "Release")
            }
            setUserId(getAppUserUseCase()?.email ?: "not_logged_in")
        }
    }

    private fun setupRemoteConfig() = launch(IO) {

        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 10 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate().await()

        val updateAvailable = remoteConfig.getLong("latestVersion").toInt() > BuildConfig.VERSION_CODE
        _remoteConfigValues.tryEmit(
            RemoteConfigValues(
                remoteConfig.getLong("blockBelow").toInt() > BuildConfig.VERSION_CODE,
                updateAvailable,
                remoteConfig.getString("contactEmail"),
                remoteConfig.getString("playStoreAppLink")
            )
        )

        setupUpdateAvailable(updateAvailable)
    }

    /**
     * Agenda atraves das preferencias o alerta de atualização disponivel para daqui a algumas horas.
     *
     * Assim que o firebase notifica que tem uma atualização disponivel deve-se esperar pelo menos 1 hora
     * antes de avisar o usuario pos as atualizações  levam pelo menos esse tempo para ficarem disponíveis na loja
     * podendo demorar mais. Afim de evitar falsos positivos, agenda-se o alerta para algumas horas depois, para garantir que a
     * atualização já esteja disponível a loja quando o usuario for avisado.
     */
    private fun setupUpdateAvailable(updateAvailable: Boolean) = launch(IO) {

        Log.d("USUK", "App.setupUpdateAvailable: updateAvailable: $updateAvailable")

        if (!updateAvailable) {
            PreferencesImpl.showUpdateDialogAtDate.reset()
            return@launch
        }
        Log.d("USUK", "App.setupUpdateAvailable: updateAvailable: ${PreferencesImpl.showUpdateDialogAtDate.value}")
        with(PreferencesImpl.showUpdateDialogAtDate) {
            if (isDefault()) invoke(
                if (BuildConfig.DEBUG)
                    System.currentTimeMillis() + 5_000L
                else System.currentTimeMillis() + 2 * 60 * 60 * 1_000L /*2 horas*/
            )
        }

    }
}

