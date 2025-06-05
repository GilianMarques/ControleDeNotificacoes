package dev.gmarques.controledenotificacoes.presentation.model

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
    val isBeingManaged: Boolean,
    val uninstalled: Boolean = false,
) : Serializable {
    companion object {

        /**
         * Cria uma instancia de [InstalledApp] para um aplicativo que foi desinstalado.
         *
         * @param targetPackage O nome do pacote do aplicativo desinstalado.
         * @return Uma instancia de [InstalledApp] representando o aplicativo desinstalado.
         */
        fun uninstalledApp(targetPackage: String): InstalledApp {
            return InstalledApp(
                extractNameFromPkg(targetPackage),
                targetPackage,
                true,
                true,
            )
        }

        /**
         * Extrai um nome de aplicativo amigável do nome do pacote.
         * Por exemplo, "com.example.myapp" se tornaria "Myapp".
         *
         * @param pkg O nome do pacote do aplicativo.
         * @return Um nome de aplicativo amigável ou o nome do pacote original se ocorrer um erro.
         */
        private fun extractNameFromPkg(pkg: String): String {
            return try {
                pkg.split(".").last().lowercase().replaceFirstChar { it.uppercase() }
            } catch (ex: Exception) {
                pkg
            }
        }


    }
}