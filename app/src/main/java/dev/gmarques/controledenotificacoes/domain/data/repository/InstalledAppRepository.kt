package dev.gmarques.controledenotificacoes.domain.data.repository

import android.graphics.drawable.Drawable
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp


/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 14:56.
 * Interface da camada de domínio responsável por fornecer apps instalados no dispositivo.
 */
interface InstalledAppRepository {


    /**
     * Recupera a lista de aplicativos instalados no dispositivo, com a opção de filtrar e excluir resultados.
     *
     * @param targetName O nome (ou parte do nome) do aplicativo a ser usado como filtro. A busca ignora
     *   maiúsculas e minúsculas. Se uma string vazia for fornecida, todos os aplicativos (exceto os do sistema)
     *   serão retornados.
     * @param excludePackages Um conjunto de IDs de pacotes que serão excluídos da lista de resultados.
     *   Além disso, outros critérios de exclusão podem ser aplicados internamente.
     * @return Uma lista de [InstalledApp]s representando os aplicativos que correspondem aos critérios,
     *   ordenada alfabeticamente pelo nome do aplicativo. Retorna uma lista vazia se nenhum aplicativo
     *   corresponder.
     */
    suspend fun getInstalledApps(
        targetName: String,
        includeSystemApps: Boolean,
        includeManagedApps: Boolean,
        excludePackages: HashSet<String>,
    ): List<InstalledApp>

    suspend fun getDrawable(pkg: String): Drawable?

    suspend fun getInstalledApp(pkg: String): InstalledApp?

}