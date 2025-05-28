package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.android.EntryPointAccessors
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.domain.framework.notification_service.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmsOnBootUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.CheckAppInBlockPeriodUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.NextAppUnlockTimeUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleDescriptionUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import dev.gmarques.controledenotificacoes.framework.ScheduleManagerImpl
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
    private inline fun <reified T> entryPoint(): T =
        EntryPointAccessors.fromApplication(App.context, T::class.java)


    override fun reportNotificationManager(): ReportNotificationManager =
        entryPoint<FrameworkEntryPoint>().reportNotificationManager()

    override fun ruleEnforcer(): RuleEnforcer =
        entryPoint<FrameworkEntryPoint>().ruleEnforcer()

    override fun scheduleManager(): ScheduleManagerImpl =
        entryPoint<FrameworkEntryPoint>().scheduleManager()

    override fun getAppUserUseCase(): GetUserUseCase =
        entryPoint<UseCasesEntryPoint>().getAppUserUseCase()

    override fun rescheduleAlarmsOnBootUseCase(): RescheduleAlarmsOnBootUseCase =
        entryPoint<UseCasesEntryPoint>().rescheduleAlarmsOnBootUseCase()

    override fun nextAppUnlockUseCase(): NextAppUnlockTimeUseCase =
        entryPoint<UseCasesEntryPoint>().nextAppUnlockUseCase()

    override fun checkAppInBlockPeriodUseCase(): CheckAppInBlockPeriodUseCase =
        entryPoint<UseCasesEntryPoint>().checkAppInBlockPeriodUseCase()

    override fun generateRuleNameUseCase(): GenerateRuleDescriptionUseCase =
        entryPoint<UseCasesEntryPoint>().generateRuleNameUseCase()

}