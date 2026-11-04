package com.example.petapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Agendamento
import com.example.petapp.data.Pet
import com.example.petapp.data.Servicos
import com.example.petapp.data.fatorPreco
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalheAgendamentoUiState(
    val agendamento: Agendamento? = null,
    val pet: Pet? = null,
    val excluido: Boolean = false
) {
    val servicoPrincipal: String get() = agendamento?.servico?.split(",")?.firstOrNull()?.trim() ?: ""

    val precoBaseTotal: Double
        get() = agendamento?.servico?.split(",")?.map { it.trim() }?.sumOf { Servicos.precosBase[it] ?: 0.0 } ?: 0.0

    val totalFinal: Double get() = precoBaseTotal * (pet?.fatorPreco() ?: 1.0)
}

class DetalheAgendamentoViewModel(
    private val agendamentoRepository: AgendamentoRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalheAgendamentoUiState())
    val uiState: StateFlow<DetalheAgendamentoUiState> = _uiState.asStateFlow()

    private var idCarregado = -1
    private var observacao: Job? = null

    fun carregar(agendamentoId: Int) {
        if (agendamentoId == idCarregado) return
        idCarregado = agendamentoId
        observacao?.cancel()
        observacao = viewModelScope.launch {
            agendamentoRepository.observarPorId(agendamentoId)
                .catch { e -> Log.e("DetalheAgendamentoVM", "Erro ao buscar agendamento", e) }
                .collect { ag ->
                    val pet = try {
                        if (ag != null) petRepository.buscarPorId(ag.petId) else null
                    } catch (e: Exception) {
                        Log.e("DetalheAgendamentoVM", "Erro ao buscar pet", e)
                        null
                    }
                    _uiState.update { it.copy(agendamento = ag, pet = pet) }
                }
        }
    }

    fun alternarConcluido() {
        val ag = _uiState.value.agendamento ?: return
        viewModelScope.launch {
            try {
                agendamentoRepository.atualizar(ag.copy(concluido = !ag.concluido))
            } catch (e: Exception) {
                Log.e("DetalheAgendamentoVM", "Erro ao atualizar", e)
            }
        }
    }

    fun cancelarAgendamento() {
        val ag = _uiState.value.agendamento ?: return
        viewModelScope.launch {
            try {
                agendamentoRepository.deletar(ag)
                _uiState.update { it.copy(excluido = true) }
            } catch (e: Exception) {
                Log.e("DetalheAgendamentoVM", "Erro ao remover", e)
            }
        }
    }

    fun navegacaoConcluida() = _uiState.update { it.copy(excluido = false) }
}
