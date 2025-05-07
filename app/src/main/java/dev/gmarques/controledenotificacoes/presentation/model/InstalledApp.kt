package dev.gmarques.controledenotificacoes.presentation.model

import android.graphics.drawable.Drawable
import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 08:50.
 * Representa um aplicativo instalado no dispositivo do usuário.
 * Esse modelo é usado pelo repositório de aplicativos instalados para exibir as informações
 * na interface. Ele nunca é escrito no banco de dados.
 */
data class InstalledApp(
    val name: String,
    val packageId: String,
) : Serializable