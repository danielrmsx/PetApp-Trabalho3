package com.example.petapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Usuario
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import com.example.petapp.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContaUiState(
    val usuario: Usuario? = null,
    val totalPets: Int = 0,
    val pendentes: Int = 0,
    val concluidos: Int = 0
) {
    val primeiroNome: String get() = usuario?.nome?.substringBefore(" ") ?: "Usuário"
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
            combine(
                usuarioRepository.observarUsuarioLogado(),
                petRepository.buscarTodos(),
                agendamentoRepository.buscarTodos()
            ) { usuario, pets, ags ->
                ContaUiState(
                    usuario = usuario,
                    totalPets = pets.size,
                    pendentes = ags.count { !it.concluido },
                    concluidos = ags.count { it.concluido }
                )
            }
                .catch { e -> Log.e("ContaViewModel", "Erro ao carregar", e) }
                .collect { novoEstado -> _uiState.value = novoEstado }
        }
    }

    fun sair() = usuarioRepository.encerrarSessao()
}
