package dev.gmarques.controledenotificacoes.domain.repository

import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp


/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 14:56.
 * Interface da camada de domínio responsável por fornecer apps instalados no dispositivo.
 */
interface AppRepository {


    /**
     * Recupera uma lista de aplicativos instalados no dispositivo.
     *
     * Esta função retorna uma lista de objetos [InstalledApp] representando os aplicativos instalados no dispositivo.
     * Você pode filtrar os resultados fornecendo um `targetName`.
     *
     * @param targetName A string usada para filtrar os aplicativos pelo nome.
     *                   Se fornecida, apenas os aplicativos cujo nome começa com esta string serão retornados.
     *                   Se a string estiver vazia, todos os aplicativos instalados serão retornados.
     *
     * @return Uma lista de objetos [InstalledApp] que correspondem aos critérios especificados.
     *         Retorna uma lista vazia se nenhum aplicativo correspondente for encontrado.
     */
    suspend fun getInstalledApps(targetName: String): List<InstalledApp>
}