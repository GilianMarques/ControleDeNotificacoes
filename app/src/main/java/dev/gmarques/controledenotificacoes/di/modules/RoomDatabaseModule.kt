package dev.gmarques.controledenotificacoes.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@Module
@InstallIn(SingletonComponent::class) // Banco de dados deve ter escopo Singleton
object RoomDatabaseModule {


    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context): RoomDatabase {
        return Room.databaseBuilder(context, RoomDatabase::class.java, "room_database").build()
    }

    @Provides
    fun provideRuleDao(roomDatabase: RoomDatabase): RuleDao {
        return roomDatabase.ruleDao()
    }

    @Provides
    fun provideManagedAppDao(roomDatabase: RoomDatabase): ManagedAppDao {
        return roomDatabase.managedAppDao()
    }
}


