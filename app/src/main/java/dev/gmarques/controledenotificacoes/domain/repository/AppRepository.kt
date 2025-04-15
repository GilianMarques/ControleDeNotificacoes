package dev.gmarques.controledenotificacoes.domain.repository

import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp


/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 14:56.
 * Interface da camada de domínio responsável por fornecer apps instalados no dispositivo.
 */
interface AppRepository {

    /**
     * Retorna todos os aplicativos instalados que podem ser iniciados pelo usuário.
     */
    suspend fun getInstalledApps(): List<InstalledApp>
}