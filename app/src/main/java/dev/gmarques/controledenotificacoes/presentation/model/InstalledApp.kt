package dev.gmarques.controledenotificacoes.presentation.model

import android.graphics.drawable.Drawable

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 08:50.
 */
data class InstalledApp(
    val name: String,
    val packageId: String,
    val icon: Drawable
)