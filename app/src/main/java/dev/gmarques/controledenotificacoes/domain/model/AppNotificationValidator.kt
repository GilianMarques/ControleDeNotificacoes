package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.model.AppNotificationValidator.AppNotificationValidatorException.BlankIdException


object AppNotificationValidator {
    fun validate(notification: AppNotification) {
        ManagedAppValidator.validatePackageId(notification.packageId).getOrThrow()
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
     * @throws BlankIdException se a string de entrada estiver vazia.
     */
    fun validateRuleId(ruleId: String): Result<String> {
        return if (ruleId.isEmpty()) Result.failure(BlankIdException())
        else Result.success(ruleId)
    }

    /**
     * Criado por Gilian Marques
     * Em 20/06/2025 as 17:13
     */
    sealed class AppNotificationValidatorException {

        class BlankIdException() :
            Exception("Em hipótese alguma a id de um objeto pode ficar vazia. Ela é gerada automaticamente e imutavel, por tanto algo deu muito errado pra isso acontecer.")
    }
}