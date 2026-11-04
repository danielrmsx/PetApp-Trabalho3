package com.example.petapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Usuario::class, Pet::class, Petshop::class, Agendamento::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun petDao(): PetDao
    abstract fun petshopDao(): PetshopDao
    abstract fun agendamentoDao(): AgendamentoDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obterBanco(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "petapp.db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCIA = it }
            }
        }
    }
}
