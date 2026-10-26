package com.example.petapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Usuario
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import com.example.petapp.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContaUiState(
    val usuario: Usuario? = null,
    val totalPets: Int = 0,
    val pendentes: Int = 0,
    val concluidos: Int = 0
) {
    fun primeiroNome(): String = usuario?.nome?.substringBefore(" ") ?: "Usuário"
}

class ContaViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val petRepository: PetRepository,
    private val agendamentoRepository: AgendamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContaUiState())
    val uiState: StateFlow<ContaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            usuarioRepository.buscarTodos().collect { lista ->
                val logado = lista.find { it.id == usuarioRepository.idUsuarioLogado() }
                _uiState.value = _uiState.value.copy(usuario = logado)
            }
        }
        viewModelScope.launch {
            petRepository.buscarTodos().collect { lista ->
                _uiState.value = _uiState.value.copy(totalPets = lista.size)
            }
        }
        viewModelScope.launch {
            agendamentoRepository.buscarTodos().collect { lista ->
                _uiState.value = _uiState.value.copy(
                    pendentes = lista.count { !it.concluido },
                    concluidos = lista.count { it.concluido }
                )
            }
        }
    }

    fun sair() {
        usuarioRepository.encerrarSessao()
    }
}
