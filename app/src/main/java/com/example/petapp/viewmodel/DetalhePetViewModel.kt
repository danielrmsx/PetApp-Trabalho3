package com.example.petapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Agendamento
import com.example.petapp.data.Pet
import com.example.petapp.data.Servicos
import com.example.petapp.data.precoDoServico
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalhePetUiState(
    val pet: Pet? = null,
    val agendamentosDoPet: List<Agendamento> = emptyList(),
    val pesoTexto: String = "",
    val erroPeso: String = ""
) {
    val precos: List<Pair<String, Double>>
        get() = pet?.let { p -> Servicos.precosBase.keys.map { servico -> servico to p.precoDoServico(servico) } } ?: emptyList()
}

class DetalhePetViewModel(
    private val petRepository: PetRepository,
    private val agendamentoRepository: AgendamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalhePetUiState())
    val uiState: StateFlow<DetalhePetUiState> = _uiState.asStateFlow()

    private var idCarregado = -1
    private var observacao: Job? = null

    fun carregar(petId: Int) {
        if (petId == idCarregado) return
        idCarregado = petId
        observacao?.cancel()
        observacao = viewModelScope.launch {
            combine(petRepository.observarPorId(petId), agendamentoRepository.observarPorPetId(petId)) { pet, ags -> pet to ags }
                .catch { e -> Log.e("DetalhePetViewModel", "Erro ao buscar pet", e) }
                .collect { (pet, ags) ->
                    _uiState.update { atual ->
                        atual.copy(
                            pet = pet,
                            agendamentosDoPet = ags,
                            pesoTexto = if (atual.pesoTexto.isEmpty()) pet?.pesoKg?.toString()?.replace('.', ',') ?: "" else atual.pesoTexto
                        )
                    }
                }
        }
    }

    fun alterarPeso(texto: String) = _uiState.update { it.copy(pesoTexto = texto.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(5)) }

    fun salvarPeso() {
        val pet = _uiState.value.pet ?: return
        val novo = _uiState.value.pesoTexto.replace(',', '.').toDoubleOrNull()
        if (novo == null || novo <= 0.0 || novo > 120.0) {
            _uiState.update { it.copy(erroPeso = "Informe um peso válido (até 120 kg).") }
            return
        }
        viewModelScope.launch {
            try {
                petRepository.atualizar(pet.copy(pesoKg = novo))
                _uiState.update { it.copy(erroPeso = "") }
            } catch (e: Exception) {
                Log.e("DetalhePetViewModel", "Erro ao salvar peso", e)
            }
        }
    }
}
