package com.example.petapp.viewmodel

import android.util.Log
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InicioUiState(
    val usuario: Usuario? = null,
    val pets: List<Pet> = emptyList(),
    val agendamentos: List<Agendamento> = emptyList(),
    val petAtualId: Int = -1,
    val mostrarNotificacoes: Boolean = false,
    val mostrarListaPets: Boolean = false
) {
    val petAtual: Pet? get() = pets.find { it.id == petAtualId } ?: pets.firstOrNull()
    val pendentes: Int get() = agendamentos.count { !it.concluido }
    val primeiroNome: String get() = usuario?.nome?.substringBefore(" ") ?: "Usuário"
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
            combine(
                usuarioRepository.observarUsuarioLogado(),
                petRepository.buscarTodos(),
                agendamentoRepository.buscarTodos()
            ) { usuario, pets, agendamentos -> Triple(usuario, pets, agendamentos) }
                .catch { e -> Log.e("InicioViewModel", "Erro ao carregar dados", e) }
                .collect { (usuario, pets, agendamentos) ->
                    _uiState.update { atual ->
                        atual.copy(
                            usuario = usuario,
                            pets = pets,
                            agendamentos = agendamentos,
                            petAtualId = if (atual.petAtualId == -1 && pets.isNotEmpty()) pets.first().id else atual.petAtualId
                        )
                    }
                }
        }
    }

    fun trocarPet() {
        val estado = _uiState.value
        val atual = estado.petAtual ?: return
        if (estado.pets.size == 2) {
            val outro = estado.pets.find { it.id != atual.id }
            if (outro != null) _uiState.update { it.copy(petAtualId = outro.id) }
        } else {
            _uiState.update { it.copy(mostrarListaPets = true) }
        }
    }

    fun selecionarPet(id: Int) = _uiState.update { it.copy(petAtualId = id, mostrarListaPets = false) }
    fun fecharListaPets() = _uiState.update { it.copy(mostrarListaPets = false) }
    fun abrirNotificacoes() = _uiState.update { it.copy(mostrarNotificacoes = true) }
    fun fecharNotificacoes() = _uiState.update { it.copy(mostrarNotificacoes = false) }
}
