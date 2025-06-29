package dev.gmarques.controledenotificacoes.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.data.local.room.dao.AppNotificationDao
import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao
import dev.gmarques.controledenotificacoes.data.local.room.migrations.MIGRATION_1_2
import dev.gmarques.controledenotificacoes.data.local.room.migrations.MIGRATION_2_3
import javax.inject.Singleton

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@Module
@InstallIn(SingletonComponent::class)
object RoomDatabaseModule {


    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context): RoomDatabase {

        return Room.databaseBuilder(context, RoomDatabase::class.java, "room_database")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigrationOnDowngrade(BuildConfig.DEBUG)
            .build()
    }

    @Provides
    fun provideRuleDao(roomDatabase: RoomDatabase): RuleDao {
        return roomDatabase.ruleDao()
    }

    @Provides
    fun provideManagedAppDao(roomDatabase: RoomDatabase): ManagedAppDao {
        return roomDatabase.managedAppDao()
    }

    @Provides
    fun provideAppNotificationDao(roomDatabase: RoomDatabase): AppNotificationDao {
        return roomDatabase.appNotificationDao()
    }
}


