package com.example.adminapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.adminapp.database.dao.RecordDao
import com.example.adminapp.database.entity.Record

/**
 * Base de datos Room de la aplicación.
 *
 * - Inicia completamente vacía: no hay callbacks de seed, no hay datos precargados.
 * - fallbackToDestructiveMigration: en desarrollo, borra y recrea al cambiar el esquema.
 *   En producción real, reemplazar con migraciones explícitas.
 */
@Database(
    entities = [Record::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "admin_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
