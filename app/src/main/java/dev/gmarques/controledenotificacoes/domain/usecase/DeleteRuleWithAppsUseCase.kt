package dev.gmarques.controledenotificacoes.domain.usecase

import android.util.Log
import androidx.room.withTransaction
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.DeleteManagedAppAndItsNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppsByRuleIdUseCase
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 02 de maio de 2025 às 23:01.
 *
 * Remove uma regra e todos os aplicativos associados de forma atômica.
 * Se qualquer operação falhar, nenhuma alteração será aplicada.
 */
class DeleteRuleWithAppsUseCase @Inject constructor(
    private val roomDb: RoomDatabase,
    private val ruleRepository: RuleRepository,
    private val deleteManagedAppAndItsNotificationsUseCase: DeleteManagedAppAndItsNotificationsUseCase,
    private val getManagedAppsByRuleIdUseCase: GetManagedAppsByRuleIdUseCase,
) {
    suspend operator fun invoke(rule: Rule): Boolean {
        return try {
            // se outras corrotinas forem iniciadas dentro do withTransaction ou de codigo dentro dos usecases chamdos aqui
            // as operaçoes feitas no DB ja nao estarao garantidas pela transação. use funções suspensas em serie para garantir atomicidade
            roomDb.withTransaction {

                getManagedAppsByRuleIdUseCase(rule.id).forEach { app ->
                    app?.let { deleteManagedAppAndItsNotificationsUseCase(app.packageId, false) }
                }

                ruleRepository.deleteRule(rule)
                with(PreferencesImpl.lastSelectedRule) {
                    if (value == rule.id) reset()// estou removendo a ultima regra selecionada? Devo limpar a preferencia
                }
            }
            true
        } catch (e: Exception) {
            Log.e("USUK", "DeleteRuleWithAppsUseCase.invoke: Falha na transação: ${e.message}")
            false
        }
    }


}
