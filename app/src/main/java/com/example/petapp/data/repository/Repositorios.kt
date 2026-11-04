package com.example.petapp.data.repository

import android.content.Context
import com.example.petapp.data.AppDatabase

class Repositorios private constructor(context: Context) {

    private val banco = AppDatabase.obterBanco(context)
    private val prefs = context.getSharedPreferences("petapp_prefs", Context.MODE_PRIVATE)

    val usuarioRepository = UsuarioRepository(banco.usuarioDao(), prefs)
    val petRepository = PetRepository(banco.petDao())
    val petshopRepository = PetshopRepository(banco.petshopDao())
    val agendamentoRepository = AgendamentoRepository(banco.agendamentoDao())

    companion object {
        @Volatile
        private var INSTANCIA: Repositorios? = null

        fun obter(context: Context): Repositorios {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Repositorios(context.applicationContext).also { INSTANCIA = it }
            }
        }
    }
}
