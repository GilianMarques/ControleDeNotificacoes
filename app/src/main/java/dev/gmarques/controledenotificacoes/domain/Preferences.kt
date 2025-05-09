package dev.gmarques.controledenotificacoes.domain

import androidx.annotation.Keep

@Keep
object Preferences {

    // variaveis com nome (nome da variavel, nao o valor) começando com show_hint podem ser limpos por opção do usuario
    const val SHOW_HINT_EDIT_RULE_1 = "hint_edit_rule_1"
    const val SHOW_HINT_SELECTED_APPS_ALREADY_MANAGED = "hint_selected_apps_already_managed"

    const val PREF_INCLUDE_SYSTEM_APPS = "pref_include_system_apps"
    const val PREF_INCLUDE_MANAGED_APPS = "pref_include_managed_apps"

    const val SHOW_DIALOG_NOT_PERMISSION_DENIED = "dialog_not_permission_denied"


}