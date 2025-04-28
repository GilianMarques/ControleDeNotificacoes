package dev.gmarques.controledenotificacoes

import android.app.Application
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

        super.onCreate()
    }
}