package dev.gmarques.controledenotificacoes.framework.model


data class RemoteConfigValues(
    val blockApp: Boolean,
    val contactEmail: String? = null,
    val playStoreAppLink: String? = null,
)