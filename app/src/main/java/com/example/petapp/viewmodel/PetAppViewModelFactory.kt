package com.example.petapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petapp.data.AppDatabase
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import com.example.petapp.data.repository.PetshopRepository
import com.example.petapp.data.repository.UsuarioRepository

class PetAppViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val banco = AppDatabase.obterBanco(context)
    private val prefs = context.getSharedPreferences("petapp_prefs", Context.MODE_PRIVATE)

    private val usuarioRepository = UsuarioRepository(banco.usuarioDao(), prefs)
    private val petRepository = PetRepository(banco.petDao())
    private val petshopRepository = PetshopRepository(banco.petshopDao())
    private val agendamentoRepository = AgendamentoRepository(banco.agendamentoDao())

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = when (modelClass) {
            LoginViewModel::class.java -> LoginViewModel(usuarioRepository)
            CadastroViewModel::class.java -> CadastroViewModel(usuarioRepository)
            InicioViewModel::class.java -> InicioViewModel(
                usuarioRepository,
                petRepository,
                agendamentoRepository
            )

            PetsViewModel::class.java -> PetsViewModel(petRepository, agendamentoRepository)
            DetalhePetViewModel::class.java -> DetalhePetViewModel(
                petRepository,
                agendamentoRepository
            )

            AgendaViewModel::class.java -> AgendaViewModel(
                usuarioRepository,
                petRepository,
                petshopRepository,
                agendamentoRepository
            )

            DetalheAgendamentoViewModel::class.java -> DetalheAgendamentoViewModel(
                agendamentoRepository,
                petRepository
            )

            ContaViewModel::class.java -> ContaViewModel(
                usuarioRepository,
                petRepository,
                agendamentoRepository
            )

            EditarPerfilViewModel::class.java -> EditarPerfilViewModel(usuarioRepository)
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
