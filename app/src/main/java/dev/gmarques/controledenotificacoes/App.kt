package dev.gmarques.controledenotificacoes

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.usecase.GetUserUseCase

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

        val entryPoint = EntryPointAccessors.fromApplication(this@App, AppEntryPoint::class.java)
        val getAppUserUseCase = entryPoint.getAppUserUseCase()

        FirebaseCrashlytics.getInstance().apply {
            setCustomKeys {
                key("environment", if (BuildConfig.DEBUG) "Debug" else "Release")
            }
            setUserId(getAppUserUseCase()?.email ?: "not_logged_in")
        }
    }


}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun getAppUserUseCase(): GetUserUseCase
}