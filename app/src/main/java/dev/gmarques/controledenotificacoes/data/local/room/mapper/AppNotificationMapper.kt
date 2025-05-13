package dev.gmarques.controledenotificacoes.data.local.room.mapper

import dev.gmarques.controledenotificacoes.data.local.room.entities.AppNotificationEntity
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.validators.AppNotificationValidator

object AppNotificationMapper {

    fun toEntity(model: AppNotification): AppNotificationEntity {

        AppNotificationValidator.validate(model)

        return AppNotificationEntity(model.packageId, model.title, model.content, model.timestamp)
    }

    fun toModel(entity: AppNotificationEntity): AppNotification {
        return AppNotification(entity.packageId, entity.title, entity.content, entity.timestamp)
    }
}