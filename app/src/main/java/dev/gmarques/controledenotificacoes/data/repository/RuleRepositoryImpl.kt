package dev.gmarques.controledenotificacoes.data.repository

import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.RuleMapper
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.validators.RuleValidator
import dev.gmarques.controledenotificacoes.domain.repository.RuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
class RuleRepositoryImpl @Inject constructor(private val ruleDao: RuleDao) : RuleRepository {

    override suspend fun addRuleOrThrow(rule: Rule) {
        RuleValidator.validate(rule)
        ruleDao.insertRuleOrThrow(RuleMapper.mapToEntity(rule))
    }

    override suspend fun updateRuleOrThrow(rule: Rule) {
        RuleValidator.validate(rule)
        ruleDao.updateRuleOrThrow(RuleMapper.mapToEntity(rule))
    }

    override suspend fun removeRule(rule: Rule) {
        ruleDao.deleteRule(RuleMapper.mapToEntity(rule))
    }

    override suspend fun getRuleById(id: String): Rule? {
        return ruleDao.getRuleById(id)?.let { RuleMapper.mapToModel(it) }
    }

    override suspend fun getAllRules(): List<Rule> {
        return ruleDao.getAllRules().map { RuleMapper.mapToModel(it) }
    }

    override fun observeAllRules(): Flow<List<Rule>> {
        return ruleDao.observeAllRules().map { entities ->
            entities.map { RuleMapper.mapToModel(it) }
        }
    }
}
