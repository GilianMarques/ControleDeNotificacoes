package dev.gmarques.controledenotificacoes.domain.data

import androidx.annotation.Keep

@Keep
interface Preferences {

    val prefIncludeSystemApps: PreferenceProperty<Boolean>
    val prefIncludeManagedApps: PreferenceProperty<Boolean>

    val scheduledAlarms: PreferenceProperty<String>
    val lastSelectedRule: PreferenceProperty<String>

    val showDialogNotPermissionDenied: PreferenceProperty<Boolean>
    val showWarningCardPostNotification: PreferenceProperty<Boolean>
    val showWarningCardBatteryRestriction: PreferenceProperty<Boolean>

    @Keep
    /**Todas as preferencias dentro dessa interface podem ser resetadas pelo usuario*/
    interface ResettableDialogHints {
        val showHintEditFirstRule: PreferenceProperty<Boolean>
        val showHintHowRulesAndManagedAppsWork: PreferenceProperty<Boolean>
        val showHintSelectedAppsAlreadyManaged: PreferenceProperty<Boolean>
    }

}






