package dev.gmarques.controledenotificacoes.data.local

import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.data.PreferenceProperty
import dev.gmarques.controledenotificacoes.domain.data.Preferences

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 28 de maio de 2025 as 20:43.
 *
 * Implementa [Preferences] que usa [PreferenceProperty] para  facilitar o acesso a leitura e escrita das preferências através dos usecases
 * [dev.gmarques.controledenotificacoes.domain.usecase.preferences.ReadPreferenceUseCase] e [dev.gmarques.controledenotificacoes.domain.usecase.preferences.SavePreferenceUseCase]
 *
 *
 */
object PreferencesImpl : Preferences {

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
            key = "scheduled_alarms",
            defaultValue = "",
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val lastSelectedRule: PreferenceProperty<String> by lazy {
        PreferenceProperty(
            key = "last_selected_rule",
            defaultValue = "null",
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val showUpdateDialogAtDate: PreferenceProperty<Long> by lazy {
        PreferenceProperty(
            key = "show_update_dialog_at_date",
            defaultValue = -1L,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
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
}