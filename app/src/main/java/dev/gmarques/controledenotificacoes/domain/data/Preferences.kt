package dev.gmarques.controledenotificacoes.domain.data

import androidx.annotation.Keep

@Keep
interface Preferences {

    // variaveis com nome (nome da variavel, nao o valor) começando com show_hint podem ser limpos por opção do usuario através de reflexão
    val showHintEditFirstRule: PreferenceProperty<Boolean>
    val showHintHowRulesAndManagedAppsWork: PreferenceProperty<Boolean>
    val showHintSelectedAppsAlreadyManaged: PreferenceProperty<Boolean>

    val prefIncludeSystemApps: PreferenceProperty<Boolean>
    val prefIncludeManagedApps: PreferenceProperty<Boolean>

    val scheduledAlarms: PreferenceProperty<String>
    val lastSelectedRule: PreferenceProperty<String>
    val showUpdateDialogAtDate: PreferenceProperty<Long>

    val showDialogNotPermissionDenied: PreferenceProperty<Boolean>
    val showWarningCardPostNotification: PreferenceProperty<Boolean>

}