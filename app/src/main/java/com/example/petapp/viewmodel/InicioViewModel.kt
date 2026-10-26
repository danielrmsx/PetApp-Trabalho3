package com.example.petapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Agendamento
import com.example.petapp.data.Pet
import com.example.petapp.data.Usuario
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import com.example.petapp.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InicioUiState(
    val usuario: Usuario? = null,
    val pets: List<Pet> = emptyList(),
    val agendamentos: List<Agendamento> = emptyList(),
    val petAtualId: Int = -1
) {
    fun petAtual(): Pet? = pets.find { it.id == petAtualId } ?: pets.firstOrNull()
    fun pendentes(): Int = agendamentos.count { !it.concluido }
    fun primeiroNome(): String = usuario?.nome?.substringBefore(" ") ?: "Usuário"
    fun pendentesDoPet(petId: Int): Int = agendamentos.count { !it.concluido && it.petId == petId }
    fun nomeDoPet(petId: Int): String = pets.find { it.id == petId }?.nome ?: "Pet"
}

class InicioViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val petRepository: PetRepository,
    private val agendamentoRepository: AgendamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            usuarioRepository.buscarTodos().collect { lista ->
                val logado = lista.find { it.id == usuarioRepository.idUsuarioLogado() }
                _uiState.value = _uiState.value.copy(usuario = logado)
            }
        }
        viewModelScope.launch {
            petRepository.buscarTodos().collect { lista ->
                _uiState.value = _uiState.value.copy(pets = lista)
            }
        }
        viewModelScope.launch {
            agendamentoRepository.buscarTodos().collect { lista ->
                _uiState.value = _uiState.value.copy(agendamentos = lista)
            }
        }
    }

    fun trocarPet() {
        val estado = _uiState.value
        val outro = estado.pets.find { it.id != estado.petAtual()?.id }
        if (outro != null) _uiState.value = estado.copy(petAtualId = outro.id)
    }

    fun selecionarPet(id: Int) {
        _uiState.value = _uiState.value.copy(petAtualId = id)
    }
}
