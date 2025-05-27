package dev.gmarques.controledenotificacoes.domain

import androidx.annotation.Keep

@Keep
object Preferences {

    // variaveis com nome (nome da variavel, nao o valor) começando com show_hint podem ser limpos por opção do usuario através de reflexão
    const val SHOW_HINT_EDIT_RULE_1 = "hint_edit_rule_1"
    const val SHOW_HINT_HOW_RULES_AND_MANAGED_APPS_WORK = "hint_how_rules_and_managed_apps_work"
    const val SHOW_HINT_SELECTED_APPS_ALREADY_MANAGED = "hint_selected_apps_already_managed"

    const val PREF_INCLUDE_SYSTEM_APPS = "pref_include_system_apps"
    const val PREF_INCLUDE_MANAGED_APPS = "pref_include_managed_apps"

    const val SCHEDULED_ALARMS = "scheduled_alarms"
    const val LAST_SELECTED_RULE = "last_selected_rule"
    const val SHOW_UPDATE_DIALOG_AT_DATE = "show_update_dialog_at_date"

    const val SHOW_DIALOG_NOT_PERMISSION_DENIED = "dialog_not_permission_denied"
    const val SHOW_WARNING_CARD_POST_NOTIFICATION = "show_warning_card_post_notification"


}