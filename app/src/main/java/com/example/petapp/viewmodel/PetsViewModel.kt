package com.example.petapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Agendamento
import com.example.petapp.data.Pet
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val erro: String = ""
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

    fun alterarFoto(uri: String) {
        _uiState.value = _uiState.value.copy(fotoSelecionada = uri)
    }

    fun alterarNome(texto: String) {
        _uiState.value = _uiState.value.copy(nome = texto)
    }

    fun alterarRaca(texto: String) {
        _uiState.value = _uiState.value.copy(raca = texto)
    }

    fun selecionarEspecie(valor: String) {
        _uiState.value = _uiState.value.copy(especie = valor)
    }

    fun selecionarSexo(valor: String) {
        _uiState.value = _uiState.value.copy(sexo = valor)
    }

    fun alterarIdade(texto: String) {
        _uiState.value = _uiState.value.copy(idade = texto.filter { it.isDigit() }.take(2))
    }

    fun alterarPeso(texto: String) {
        _uiState.value =
            _uiState.value.copy(peso = texto.filter { it.isDigit() || it == ',' || it == '.' }
                .take(5))
    }

    fun adicionarPet() {
        val e = _uiState.value
        val idade = e.idade.toIntOrNull()
        val peso = e.peso.replace(',', '.').toDoubleOrNull()

        if (e.nome.isBlank()) {
            _uiState.value = e.copy(erro = "Informe o nome do pet.")
            return
        }
        if (e.especie.isBlank()) {
            _uiState.value = e.copy(erro = "Selecione se é Cão ou Gato.")
            return
        }
        if (e.sexo.isBlank()) {
            _uiState.value = e.copy(erro = "Selecione se é Macho ou Fêmea.")
            return
        }
        if (idade == null) {
            _uiState.value = e.copy(erro = "Informe uma idade válida.")
            return
        }
        if (peso == null) {
            _uiState.value = e.copy(erro = "Informe um peso válido.")
            return
        }

        viewModelScope.launch {
            val novoPet = Pet(
                nome = e.nome.trim(),
                especie = e.especie,
                sexo = e.sexo,
                raca = e.raca.trim().ifBlank { "SRD" },
                idadeAnos = idade,
                pesoKg = peso,
                fotoUri = e.fotoSelecionada
            )
            petRepository.inserir(novoPet)
            _uiState.value = _uiState.value.copy(
                nome = "",
                raca = "",
                especie = "",
                sexo = "",
                idade = "",
                peso = "",
                fotoSelecionada = "",
                erro = ""
            )
        }
    }

    fun remover(pet: Pet) {
        viewModelScope.launch {
            agendamentoRepository.deletarTodosDoPet(pet.id)
            petRepository.deletar(pet)
        }
    }
}
