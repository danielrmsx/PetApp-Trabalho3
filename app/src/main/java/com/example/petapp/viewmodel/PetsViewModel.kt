package com.example.petapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Agendamento
import com.example.petapp.data.Pet
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetsUiState(
    val pets: List<Pet> = emptyList(),
    val agendamentos: List<Agendamento> = emptyList(),
    val fotoSelecionada: String = "",
    val nome: String = "",
    val raca: String = "",
    val especie: String = "",
    val sexo: String = "",
    val idade: String = "",
    val peso: String = "",
    val erro: String = "",
    val petParaRemover: Pet? = null
) {
    fun pendentesDoPet(petId: Int): Int = agendamentos.count { it.petId == petId && !it.concluido }
}

class PetsViewModel(
    private val petRepository: PetRepository,
    private val agendamentoRepository: AgendamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(petRepository.buscarTodos(), agendamentoRepository.buscarTodos()) { pets, ags -> pets to ags }
                .catch { e -> Log.e("PetsViewModel", "Erro ao carregar", e) }
                .collect { (pets, ags) -> _uiState.update { it.copy(pets = pets, agendamentos = ags) } }
        }
    }

    fun alterarFoto(uri: String) = _uiState.update { it.copy(fotoSelecionada = uri) }
    fun alterarNome(texto: String) = _uiState.update { it.copy(nome = texto) }
    fun alterarRaca(texto: String) = _uiState.update { it.copy(raca = texto) }
    fun selecionarEspecie(valor: String) = _uiState.update { it.copy(especie = valor) }
    fun selecionarSexo(valor: String) = _uiState.update { it.copy(sexo = valor) }
    fun alterarIdade(texto: String) = _uiState.update { it.copy(idade = texto.filter { c -> c.isDigit() }.take(2)) }
    fun alterarPeso(texto: String) = _uiState.update { it.copy(peso = texto.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(5)) }

    fun adicionarPet() {
        val e = _uiState.value
        val idadeInt = e.idade.toIntOrNull()
        val pesoDouble = e.peso.replace(',', '.').toDoubleOrNull()
        val erro = when {
            e.nome.isBlank() -> "Informe o nome do pet."
            e.especie.isBlank() -> "Selecione se é Cão ou Gato."
            e.sexo.isBlank() -> "Selecione se é Macho ou Fêmea."
            idadeInt == null -> "Informe uma idade válida."
            pesoDouble == null -> "Informe um peso válido."
            else -> null
        }
        if (erro != null || idadeInt == null || pesoDouble == null) {
            _uiState.update { it.copy(erro = erro ?: "") }
            return
        }
        viewModelScope.launch {
            try {
                petRepository.inserir(
                    Pet(nome = e.nome.trim(), especie = e.especie, sexo = e.sexo, raca = e.raca.trim().ifBlank { "SRD" }, idadeAnos = idadeInt, pesoKg = pesoDouble, fotoUri = e.fotoSelecionada)
                )
                _uiState.update { it.copy(nome = "", raca = "", especie = "", sexo = "", idade = "", peso = "", fotoSelecionada = "", erro = "") }
            } catch (ex: Exception) {
                Log.e("PetsViewModel", "Erro ao inserir pet", ex)
                _uiState.update { it.copy(erro = "Erro ao salvar o pet.") }
            }
        }
    }

    fun pedirRemocao(pet: Pet) = _uiState.update { it.copy(petParaRemover = pet) }
    fun cancelarRemocao() = _uiState.update { it.copy(petParaRemover = null) }

    fun confirmarRemocao() {
        val pet = _uiState.value.petParaRemover ?: return
        viewModelScope.launch {
            try {
                agendamentoRepository.deletarTodosDoPet(pet.id)
                petRepository.deletar(pet)
            } catch (e: Exception) {
                Log.e("PetsViewModel", "Erro ao remover pet", e)
            }
            _uiState.update { it.copy(petParaRemover = null) }
        }
    }
}
