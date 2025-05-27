package dev.gmarques.controledenotificacoes.framework.model


data class RemoteConfigValues(
    val blockApp: Boolean,
    val hasUpdateAvailable: Boolean,
    val contactEmail: String,
    val playStoreAppLink: String,
)