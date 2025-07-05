package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.android.EntryPointAccessors
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.domain.framework.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmsOnBootUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.ScheduleAutoTurnOnUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.IsAppInBlockPeriodUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.NextAppUnlockTimeUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.ReadPreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.SavePreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleDescriptionUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveAllRulesUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import dev.gmarques.controledenotificacoes.framework.AlarmSchedulerImpl
import dev.gmarques.controledenotificacoes.framework.EchoImpl
import dev.gmarques.controledenotificacoes.framework.report_notification.ReportNotificationManager

/**
 * Criado por Gilian Marques
 * Em sábado, 24 de maio de 2025 as 16:28.
 */
object HiltEntryPoints : FrameworkEntryPoint, UseCasesEntryPoint {

    /**
     * Recupera uma instância de um EntryPoint Hilt registrado no `AndroidManifest.xml`
     * a partir do contexto da aplicação.
     *
     * Esta função é genérica e pode ser utilizada para acessar qualquer interface de
     * EntryPoint previamente definida, como por exemplo `UseCasesEntryPoint`,
     * `RuleEnforcerEntryPoint`, `ScheduleManagerEntryPoint`, etc.
     *
     * O parâmetro genérico [T] é automaticamente inferido no momento da chamada,
     * dispensando a necessidade de passar a classe explicitamente.
     *
     * Esta função simplifica o acesso ao mét.odo `EntryPointAccessors.fromApplication(...)`
     * evitando repetição de código e melhorando a legibilidade.
     *
     * @return A instância do EntryPoint correspondente ao tipo [T].
     */
    private inline fun <reified T> entryPoint(): T {
        return EntryPointAccessors.fromApplication(App.instance, T::class.java)
    }

    override fun reportNotificationManager(): ReportNotificationManager {
        return entryPoint<FrameworkEntryPoint>().reportNotificationManager()
    }

    override fun ruleEnforcer(): RuleEnforcer {
        return entryPoint<FrameworkEntryPoint>().ruleEnforcer()
    }

    override fun scheduleManager(): AlarmSchedulerImpl {
        return entryPoint<FrameworkEntryPoint>().scheduleManager()
    }

    override fun readPreferenceUseCase(): ReadPreferenceUseCase {
        return entryPoint<FrameworkEntryPoint>().readPreferenceUseCase()
    }

    override fun savePreferenceUseCase(): SavePreferenceUseCase {
        return entryPoint<FrameworkEntryPoint>().savePreferenceUseCase()
    }

    override fun echo(): EchoImpl {
        return entryPoint<FrameworkEntryPoint>().echo()
    }

    override fun getAppUserUseCase(): GetUserUseCase {
        return entryPoint<UseCasesEntryPoint>().getAppUserUseCase()
    }

    override fun rescheduleAlarmsOnBootUseCase(): RescheduleAlarmsOnBootUseCase {
        return entryPoint<UseCasesEntryPoint>().rescheduleAlarmsOnBootUseCase()
    }

    override fun nextAppUnlockUseCase(): NextAppUnlockTimeUseCase {
        return entryPoint<UseCasesEntryPoint>().nextAppUnlockUseCase()
    }

    override fun isAppInBlockPeriodUseCase(): IsAppInBlockPeriodUseCase {
        return entryPoint<UseCasesEntryPoint>().isAppInBlockPeriodUseCase()
    }

    override fun generateRuleNameUseCase(): GenerateRuleDescriptionUseCase {
        return entryPoint<UseCasesEntryPoint>().generateRuleNameUseCase()
    }

    override fun updateManagedAppUseCase(): UpdateManagedAppUseCase {
        return entryPoint<UseCasesEntryPoint>().updateManagedAppUseCase()
    }

    override fun insertAppNotificationUseCase(): InsertAppNotificationUseCase {
        return entryPoint<UseCasesEntryPoint>().insertAppNotificationUseCase()
    }

    override fun observeAllRulesUseCase(): ObserveAllRulesUseCase {
        return entryPoint<UseCasesEntryPoint>().observeAllRulesUseCase()
    }

    override fun scheduleAutoTurnOnUseCase(): ScheduleAutoTurnOnUseCase {
        return entryPoint<UseCasesEntryPoint>().scheduleAutoTurnOnUseCase()
    }

}