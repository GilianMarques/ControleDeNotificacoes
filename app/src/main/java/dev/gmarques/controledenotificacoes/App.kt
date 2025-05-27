package dev.gmarques.controledenotificacoes

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.Preferences
import dev.gmarques.controledenotificacoes.domain.usecase.settings.SavePreferenceUseCase
import dev.gmarques.controledenotificacoes.framework.model.RemoteConfigValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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

    @Inject
    lateinit var savePreferenceUseCase: SavePreferenceUseCase

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

        if (updateAvailable) setupUpdateAvailable()
    }

    /**
     * Agenda atraves das preferencias o alerta de atualização disponivel para daqui a algumas horas.
     *
     * Assim que o firebase notifica que tem uma atualização disponivel deve-se esperar pelo menos 1 hora
     * antes de avisar o usuario pos as atualizações  levam pelo menos esse tempo para ficarem disponíveis na loja
     * podendo demorar mais. Afim de evitar falsos positivos, agenda-se o alerta para algumas horas depois, para garantir que a
     * atualização já esteja disponível a loja quando o usuario for avisado.
     */
    private fun setupUpdateAvailable() = launch(IO) {

        //preciso esperar antes de fazer o agendamento para nao sobrescrever um agendamento de um alerta que
        // pode estar para ser exibiado por agora na interface
        delay(10_000)

        savePreferenceUseCase(
            Preferences.SHOW_UPDATE_DIALOG_AT_DATE, if (BuildConfig.DEBUG)
                System.currentTimeMillis() + 5_000L
            else System.currentTimeMillis() + 2 * 60 * 60 * 1_000L /*2 horas*/
        )
    }
}

