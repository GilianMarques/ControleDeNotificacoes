package dev.gmarques.controledenotificacoes.data.local

import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.data.PreferenceProperty
import dev.gmarques.controledenotificacoes.domain.data.Preferences
import dev.gmarques.controledenotificacoes.domain.data.Preferences.ResettableDialogHints

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 28 de maio de 2025 as 20:43.
 *
 * Implementa [Preferences] e [Preferences.ResettableDialogHints] que definem quais sao as preferencias disponiveis no app e faz a implementação
 * dessas preferencias usando instancias de [PreferenceProperty] inicializadas sob demanda para  facilitar o acesso, leitura e escrita
 * das preferências através dos usecases
 * [dev.gmarques.controledenotificacoes.domain.usecase.preferences.ReadPreferenceUseCase] e [dev.gmarques.controledenotificacoes.domain.usecase.preferences.SavePreferenceUseCase]
 * Isso permite acessar e modificar as preferencias de maneira simples, funcional e escalavel
 *
 */
object PreferencesImpl : Preferences, ResettableDialogHints {

    private val reader = HiltEntryPoints.readPreferenceUseCase()
    private val saver = HiltEntryPoints.savePreferenceUseCase()

    override val showHintEditFirstRule: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_hint_edit_first_rule",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }
    override val showHintSelectFirstApp: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_hint_select_first_app",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val showHintHowRulesAndManagedAppsWork: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_hint_how_rules_and_managed_apps_work",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val showHintSelectedAppsAlreadyManaged: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_hint_selected_apps_already_managed",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val prefIncludeSystemApps: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "pref_include_system_apps",
            defaultValue = false,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke,
        )
    }

    override val prefIncludeManagedApps: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "pref_include_managed_apps",
            defaultValue = false,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val scheduledAlarms: PreferenceProperty<String> by lazy {
        PreferenceProperty(
            key = "scheduled_alarms", defaultValue = "", preferenceReader = reader::invoke, preferenceSaver = saver::invoke
        )
    }

    override val lastSelectedRule: PreferenceProperty<String> by lazy {
        PreferenceProperty(
            key = "last_selected_rule", defaultValue = "null", preferenceReader = reader::invoke, preferenceSaver = saver::invoke
        )
    }

    override val showDialogNotPermissionDenied: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_dialog_not_permission_denied",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val showWarningCardPostNotification: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_warning_card_post_notification",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val echoEnabled: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "echo_enabled", defaultValue = false, preferenceReader = reader::invoke, preferenceSaver = saver::invoke
        )
    }


}