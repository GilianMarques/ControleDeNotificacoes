package dev.gmarques.controledenotificacoes

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import dagger.hilt.android.HiltAndroidApp
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@HiltAndroidApp
class App : Application() {

    companion object {
        lateinit var context: App
    }


    override fun onCreate() {
        context = this
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


}

