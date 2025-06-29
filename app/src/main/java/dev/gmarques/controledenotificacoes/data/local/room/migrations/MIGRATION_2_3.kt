package dev.gmarques.controledenotificacoes.data.local.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
Criado por Gilian Marques
 * Em 29/06/2025 as 10:54
 *
 * Essa migração adiciona uma coluna 'condition' à tabela 'rules'. Esta coluna armazenará a condição para a regra de notificação.
 */
@Suppress("ClassName")
object MIGRATION_2_3 : Migration(2, 3) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE rules ADD COLUMN condition TEXT NULL")
    }
}