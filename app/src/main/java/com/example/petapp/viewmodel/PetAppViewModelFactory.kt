package com.example.petapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import com.example.petapp.data.repository.PetshopRepository
import com.example.petapp.data.repository.UsuarioRepository

class PetAppViewModelFactory(
    private val usuarioRepository: UsuarioRepository,
    private val petRepository: PetRepository,
    private val petshopRepository: PetshopRepository,
    private val agendamentoRepository: AgendamentoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(usuarioRepository)
            modelClass.isAssignableFrom(CadastroViewModel::class.java) ->
                CadastroViewModel(usuarioRepository)
            modelClass.isAssignableFrom(InicioViewModel::class.java) ->
                InicioViewModel(usuarioRepository, petRepository, agendamentoRepository)
            modelClass.isAssignableFrom(PetsViewModel::class.java) ->
                PetsViewModel(petRepository, agendamentoRepository)
            modelClass.isAssignableFrom(DetalhePetViewModel::class.java) ->
                DetalhePetViewModel(petRepository, agendamentoRepository)
            modelClass.isAssignableFrom(AgendaViewModel::class.java) ->
                AgendaViewModel(usuarioRepository, petRepository, petshopRepository, agendamentoRepository)
            modelClass.isAssignableFrom(DetalheAgendamentoViewModel::class.java) ->
                DetalheAgendamentoViewModel(agendamentoRepository, petRepository)
            modelClass.isAssignableFrom(ContaViewModel::class.java) ->
                ContaViewModel(usuarioRepository, petRepository, agendamentoRepository)
            modelClass.isAssignableFrom(EditarPerfilViewModel::class.java) ->
                EditarPerfilViewModel(usuarioRepository)
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
        return viewModel as T
    }
}
