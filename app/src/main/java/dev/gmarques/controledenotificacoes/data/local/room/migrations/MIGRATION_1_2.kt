package dev.gmarques.controledenotificacoes.data.local.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 30 de maio de 2025 as 12:27.
 *
 * Essa migração adiciona um valor hasPendingNotifications referente ao [dev.gmarques.controledenotificacoes.domain.model.ManagedApp]
 */
@Suppress("ClassName")
object MIGRATION_1_2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE managed_apps ADD COLUMN hasPendingNotifications INTEGER NOT NULL DEFAULT 0")
    }
}