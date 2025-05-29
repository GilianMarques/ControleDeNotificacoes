package dev.gmarques.controledenotificacoes.domain.usecase

import android.util.Log
import androidx.room.withTransaction
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.model.Rule
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
    private val managedAppRepository: ManagedAppRepository,
) {
    suspend operator fun invoke(rule: Rule): Boolean {
        return try {
            roomDb.withTransaction {
                managedAppRepository.deleteManagedAppsByRuleId(rule.id)
                ruleRepository.deleteRule(rule)
            }
            true
        } catch (e: Exception) {
            Log.e("USUK", "DeleteRuleWithAppsUseCase.invoke: Falha na transação: ${e.message}")
            false
        }
    }
}
