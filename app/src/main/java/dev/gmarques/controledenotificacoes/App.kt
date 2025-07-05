package dev.gmarques.controledenotificacoes

import android.app.Application
import android.content.Intent
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.framework.model.RemoteConfigValues
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@HiltAndroidApp
class App() : Application(), CoroutineScope by MainScope() {

    companion object {
        lateinit var instance: App
    }

    private val _remoteConfigValues = MutableStateFlow<RemoteConfigValues?>(null)
    val remoteConfigValues get() = _remoteConfigValues

    override fun onCreate() {

        instance = this

        setupRemoteConfig()
        setupCrashLytics()
        startNotificationService()

        /** Vai agendar em loop um broadcast que liga o serviço de notificações */
        HiltEntryPoints.scheduleAutoTurnOnUseCase().invoke()

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

    private fun setupRemoteConfig() = CoroutineScope(IO).launch {

        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 10 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        val fetchResult = withTimeoutOrNull(5000) { // 5 segundos
            try {
                remoteConfig.fetchAndActivate().await()
                _remoteConfigValues.tryEmit(
                    RemoteConfigValues(
                        remoteConfig.getLong("blockBelow").toInt() > BuildConfig.VERSION_CODE,
                        remoteConfig.getString("contactEmail"),
                        remoteConfig.getString("playStoreAppLink")
                    )
                )
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                null
            }
        }

        if (fetchResult == null) {
            _remoteConfigValues.tryEmit(
                RemoteConfigValues(blockApp = false)
            )
        }


    }

    fun startNotificationService() {
        val serviceIntent = Intent(this, NotificationServiceManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    fun restartNotificationService() {

        val intent = Intent(this, NotificationServiceManager::class.java)
        stopService(intent)
        startNotificationService()
    }


}

