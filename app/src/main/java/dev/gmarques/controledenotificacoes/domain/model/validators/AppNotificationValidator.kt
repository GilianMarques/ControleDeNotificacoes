package dev.gmarques.controledenotificacoes.domain.model.validators

import dev.gmarques.controledenotificacoes.domain.exceptions.BlankStringException
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.validators.ManagedAppValidator.validatePackageId

object AppNotificationValidator {
    fun validate(notification: AppNotification) {
        validatePackageId(notification.packageId).getOrThrow()
    }

    /**
     * Valida um ID de regra.
     *
     * Verifica se o `ruleId` fornecido é válido, garantindo que não esteja vazio.
     *
     * @param ruleId O ID da regra a ser validado.
     * @return Um objeto `Result`.
     *         - `Result.success(ruleId)` se o `ruleId` for válido (não vazio), contendo o `ruleId`.
     *         - `Result.failure(BlankStringException)` se o `ruleId` estiver vazio, contendo a exceção `BlankStringException`.
     * @throws BlankStringException se a string de entrada estiver vazia.
     */
    fun validateRuleId(ruleId: String): Result<String> {
        return if (ruleId.isEmpty()) Result.failure(BlankStringException("A id de regra nao pode ficar em branco"))
        else Result.success(ruleId)
    }
}