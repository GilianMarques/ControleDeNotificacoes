package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.model.ManagedAppValidator.ManagedAppValidatorException.BlankPackageIdException
import dev.gmarques.controledenotificacoes.domain.model.ManagedAppValidator.ManagedAppValidatorException.BlankRuleIdException
import dev.gmarques.controledenotificacoes.domain.model.ManagedAppValidator.validatePackageId
import dev.gmarques.controledenotificacoes.domain.model.ManagedAppValidator.validateRuleId


/**
 * Criado por Gilian Marques
 * Em domingo, 13 de abril de 2025 as 16:17.
 */
object ManagedAppValidator {

    /**
     * Valida um objeto ManagedApp.
     *
     * Esta função verifica a validade das propriedades `packageId` e `ruleId` do objeto [ManagedApp] fornecido.
     * Ela utiliza as funções `validatePackageId` e `validateRuleId` para realizar a validação.
     * Caso qualquer validação falhe, uma exceção será lançada.
     *
     * @param managedApp O objeto [ManagedApp] a ser validado.
     * @throws BlankStringException Se o `packageId` ou `ruleId` for inválido.
     * @see validatePackageId
     * @see validateRuleId
     */
    fun validate(managedApp: ManagedApp) {
        validatePackageId(managedApp.packageId).getOrThrow()
        validateRuleId(managedApp.ruleId).getOrThrow()
    }

    /**
     * Valida um ID de pacote.
     *
     * Verifica se o ID de pacote fornecido não está vazio.
     * Se estiver vazio, retorna `Result.failure` com `BlankStringException`.
     * Caso contrário, retorna `Result.success` com o ID de pacote original.
     *
     * @param packageId O ID de pacote a ser validado.
     * @return Um objeto `Result`. Sucesso contém o `packageId`. Falha contém `BlankStringException`.
     * @throws BlankPackageIdException se o `packageId` for vazio.
     */
    fun validatePackageId(packageId: String): Result<String> {
        return if (packageId.isEmpty()) Result.failure(BlankPackageIdException())
        else Result.success(packageId)
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
     * @throws BlankRuleIdException se a string de entrada estiver vazia.
     */
    fun validateRuleId(ruleId: String): Result<String> {
        return if (ruleId.isEmpty()) Result.failure(BlankRuleIdException())
        else Result.success(ruleId)
    }

    /**
     * Criado por Gilian Marques
     * Em 20/06/2025 as 17:13
     */
    sealed class ManagedAppValidatorException {
        class BlankRuleIdException() :
            Exception("Em hipótese alguma a id de um objeto pode ficar vazia. Ela é gerada automaticamente e imutavel, por tanto algo deu muito errado pra isso acontecer.")

        class BlankPackageIdException() :
            Exception("Em hipótese alguma a id de um objeto pode ficar vazia. Ela é gerada automaticamente e imutavel, por tanto algo deu muito errado pra isso acontecer.")
    }

}