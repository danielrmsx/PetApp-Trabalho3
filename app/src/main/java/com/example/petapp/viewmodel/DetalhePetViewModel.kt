package com.example.petapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Agendamento
import com.example.petapp.data.Pet
import com.example.petapp.data.Servicos
import com.example.petapp.data.precoDoServico
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalhePetUiState(
    val pet: Pet? = null,
    val agendamentos: List<Agendamento> = emptyList(),
    val pesoTexto: String = "",
    val erroPeso: String = ""
) {
    fun agendamentosDoPet(): List<Agendamento> = agendamentos.filter { it.petId == pet?.id }

    fun precos(): List<Pair<String, Double>> {
        val p = pet ?: return emptyList()
        return Servicos.precosBase.keys.map { servico -> Pair(servico, p.precoDoServico(servico)) }
    }
}

class DetalhePetViewModel(
    private val petRepository: PetRepository,
    private val agendamentoRepository: AgendamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalhePetUiState())
    val uiState: StateFlow<DetalhePetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            agendamentoRepository.buscarTodos().collect { lista ->
                _uiState.value = _uiState.value.copy(agendamentos = lista)
            }
        }
    }

    fun carregar(petId: Int) {
        viewModelScope.launch {
            val pet = petRepository.buscarPorId(petId)
            val peso = pet?.pesoKg?.toString()?.replace('.', ',') ?: ""
            _uiState.value = _uiState.value.copy(pet = pet, pesoTexto = peso)
        }
    }

    fun alterarPeso(texto: String) {
        _uiState.value =
            _uiState.value.copy(pesoTexto = texto.filter { it.isDigit() || it == ',' || it == '.' }
                .take(5))
    }

    fun salvarPeso() {
        val pet = _uiState.value.pet ?: return
        val novoPeso = _uiState.value.pesoTexto.replace(',', '.').toDoubleOrNull()
        if (novoPeso == null || novoPeso <= 0.0 || novoPeso > 120.0) {
            _uiState.value = _uiState.value.copy(erroPeso = "Informe um peso válido (até 120 kg).")
            return
        }
        viewModelScope.launch {
            val petAtualizado = pet.copy(pesoKg = novoPeso)
            petRepository.atualizar(petAtualizado)
            _uiState.value = _uiState.value.copy(pet = petAtualizado, erroPeso = "")
        }
    }
}
