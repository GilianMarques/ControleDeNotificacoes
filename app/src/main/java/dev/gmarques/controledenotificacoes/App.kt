package dev.gmarques.controledenotificacoes

import android.app.Application
import android.hardware.camera2.CaptureRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
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
        FirebaseCrashlytics.getInstance().apply {

            setCustomKeys {
                key("environment", if (BuildConfig.DEBUG) "Debug" else "Release")
            }
            setUserId(FirebaseAuth.getInstance().currentUser?.email ?: "not_logged_in")
        }
    }
}