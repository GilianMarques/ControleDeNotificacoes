package dev.gmarques.controledenotificacoes.domain.data

import androidx.annotation.Keep

@Keep
interface Preferences {

    /**Se deve incluir apps do sistema na lista de seleçao de apps instalados*/
    val prefIncludeSystemApps: PreferenceProperty<Boolean>

    /**Se deve incluir apps que ja estao sendo gerenciados na lista de seleçao de apps instalados*/
    val prefIncludeManagedApps: PreferenceProperty<Boolean>

    val scheduledAlarms: PreferenceProperty<String>
    val lastSelectedRule: PreferenceProperty<String>

    val showDialogNotPermissionDenied: PreferenceProperty<Boolean>
    val showWarningCardPostNotification: PreferenceProperty<Boolean>

    val echoEnabled: PreferenceProperty<Boolean>

    @Keep
    /**Todas as preferencias dentro dessa interface podem ser resetadas pelo usuario*/
    interface ResettableDialogHints {
        val showHintEditFirstRule: PreferenceProperty<Boolean>
        val showHintSelectFirstApp: PreferenceProperty<Boolean>
        val showHintHowRulesAndManagedAppsWork: PreferenceProperty<Boolean>
        val showHintSelectedAppsAlreadyManaged: PreferenceProperty<Boolean>
    }

}






