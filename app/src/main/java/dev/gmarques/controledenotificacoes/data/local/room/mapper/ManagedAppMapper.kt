package  dev.gmarques.controledenotificacoes.data.local.room.mapper

import dev.gmarques.controledenotificacoes.data.local.room.entities.ManagedAppEntity
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp

/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 as 16:08.
 */
object ManagedAppMapper {

    /**
     * Mapeia um objeto [ManagedApp] para um objeto [ManagedAppEntity].
     *
     * Esta função recebe uma instância de [ManagedApp], que representa um aplicativo
     * gerenciado por um sistema, e a transforma em uma instância de [ManagedAppEntity],
     * adequada para persistência ou outras necessidades de armazenamento de dados.
     * Ela extrai as propriedades relevantes (ruleId e packageId) do [ManagedApp] e as utiliza
     * para construir um novo [ManagedAppEntity].
     *
     * @param managedApp O objeto [ManagedApp] a ser mapeado.
     * @return Um novo objeto [ManagedAppEntity] contendo os dados mapeados.
     */
    fun mapToEntity(managedApp: ManagedApp): ManagedAppEntity {

        return ManagedAppEntity(
            ruleId = managedApp.ruleId,
            packageId = managedApp.packageId,
            hasPendingNotifications = managedApp.hasPendingNotifications
        )
    }

    /**
     * Mapeia uma [ManagedAppEntity] para um modelo [ManagedApp].
     *
     * Esta função recebe uma [ManagedAppEntity] representando os dados persistentes
     * e a transforma em um objeto [ManagedApp] que é usado na lógica de domínio da aplicação.
     * Ela extrai os campos necessários (ruleId e packageId) da entidade e cria uma
     * instância [ManagedApp] correspondente.
     *
     * @param entity A [ManagedAppEntity] a ser mapeada.
     * @return Uma instância [ManagedApp] representando a entidade mapeada.
     */
    fun mapToModel(entity: ManagedAppEntity): ManagedApp {

        return ManagedApp(
            ruleId = entity.ruleId,
            packageId = entity.packageId,
            hasPendingNotifications = entity.hasPendingNotifications
        )
    }

}