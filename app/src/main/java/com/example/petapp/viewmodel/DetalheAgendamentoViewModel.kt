package com.example.petapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Agendamento
import com.example.petapp.data.Pet
import com.example.petapp.data.Servicos
import com.example.petapp.data.fatorPreco
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalheAgendamentoUiState(
    val agendamento: Agendamento? = null,
    val pet: Pet? = null
) {
    fun servicoPrincipal(): String = agendamento?.servico?.split(",")?.firstOrNull()?.trim() ?: ""

    fun precoBaseTotal(): Double {
        var total = 0.0
        val servicos = agendamento?.servico?.split(",") ?: emptyList()
        for (servico in servicos) {
            total += Servicos.precosBase[servico.trim()] ?: 0.0
        }
        return total
    }

    fun totalFinal(): Double = precoBaseTotal() * (pet?.fatorPreco() ?: 1.0)
}

class DetalheAgendamentoViewModel(
    private val agendamentoRepository: AgendamentoRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalheAgendamentoUiState())
    val uiState: StateFlow<DetalheAgendamentoUiState> = _uiState.asStateFlow()

    fun carregar(agendamentoId: Int) {
        viewModelScope.launch {
            val agendamento = agendamentoRepository.buscarPorId(agendamentoId)
            val pet =
                if (agendamento != null) petRepository.buscarPorId(agendamento.petId) else null
            _uiState.value = _uiState.value.copy(agendamento = agendamento, pet = pet)
        }
    }

    fun alternarConcluido() {
        val agendamento = _uiState.value.agendamento ?: return
        viewModelScope.launch {
            val atualizado = agendamento.copy(concluido = !agendamento.concluido)
            agendamentoRepository.atualizar(atualizado)
            _uiState.value = _uiState.value.copy(agendamento = atualizado)
        }
    }

    fun cancelarAgendamento(onCancelado: () -> Unit) {
        val agendamento = _uiState.value.agendamento ?: return
        viewModelScope.launch {
            agendamentoRepository.deletar(agendamento)
            onCancelado()
        }
    }
}
