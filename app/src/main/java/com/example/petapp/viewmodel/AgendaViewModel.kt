package com.example.petapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Agendamento
import com.example.petapp.data.Pet
import com.example.petapp.data.Petshop
import com.example.petapp.data.Usuario
import com.example.petapp.data.repository.AgendamentoRepository
import com.example.petapp.data.repository.PetRepository
import com.example.petapp.data.repository.PetshopRepository
import com.example.petapp.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class AgendaUiState(
    val usuario: Usuario? = null,
    val pets: List<Pet> = emptyList(),
    val petshops: List<Petshop> = emptyList(),
    val agendamentos: List<Agendamento> = emptyList(),
    val petId: Int = -1,
    val servicosSelecionados: Set<String> = emptySet(),
    val data: String = "",
    val horario: String = "",
    val obs: String = "",
    val cepBusca: String = "",
    val pesquisaServico: String = "",
    val erro: String = ""
) {
    fun petAtual(): Pet? = pets.find { it.id == petId } ?: pets.firstOrNull()
    fun agendamentosPendentes(): List<Agendamento> = agendamentos.filter { !it.concluido }
    fun agendamentosConcluidos(): List<Agendamento> = agendamentos.filter { it.concluido }

    fun petshopsFiltrados(): List<Petshop> {
        if (pesquisaServico.isBlank()) return petshops
        return petshops.filter { it.servicosText.contains(pesquisaServico, ignoreCase = true) }
    }

    fun nomeDoPet(id: Int): String = pets.find { it.id == id }?.nome ?: "Pet"
    fun nomeDoPetshop(id: Int): String = petshops.find { it.id == id }?.nome ?: "Petshop"
}

class AgendaViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val petRepository: PetRepository,
    private val petshopRepository: PetshopRepository,
    private val agendamentoRepository: AgendamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    private var iniciado = false

    init {
        viewModelScope.launch {
            petshopRepository.criarPetshopsIniciais()
        }
        viewModelScope.launch {
            usuarioRepository.buscarTodos().collect { lista ->
                val logado = lista.find { it.id == usuarioRepository.idUsuarioLogado() }
                val cep = if (_uiState.value.cepBusca.isEmpty()) logado?.cep
                    ?: "" else _uiState.value.cepBusca
                _uiState.value = _uiState.value.copy(usuario = logado, cepBusca = cep)
            }
        }
        viewModelScope.launch {
            petRepository.buscarTodos().collect { lista ->
                _uiState.value = _uiState.value.copy(pets = lista)
            }
        }
        viewModelScope.launch {
            petshopRepository.buscarTodos().collect { lista ->
                _uiState.value = _uiState.value.copy(petshops = lista)
            }
        }
        viewModelScope.launch {
            agendamentoRepository.buscarTodos().collect { lista ->
                _uiState.value = _uiState.value.copy(agendamentos = lista)
            }
        }
    }

    fun iniciar(servicoInicial: String, petInicial: Int) {
        if (iniciado) return
        iniciado = true
        val servicos = if (servicoInicial.isNotBlank()) setOf(servicoInicial) else setOf("Banho")
        val petId = if (petInicial > 0) petInicial else _uiState.value.petId
        _uiState.value = _uiState.value.copy(servicosSelecionados = servicos, petId = petId)
    }

    fun alterarCep(texto: String) {
        val numeros = texto.filter { it.isDigit() }
        if (numeros.length <= 8) _uiState.value = _uiState.value.copy(cepBusca = numeros)
    }

    fun alterarPesquisa(texto: String) {
        _uiState.value = _uiState.value.copy(pesquisaServico = texto)
    }

    fun selecionarPet(id: Int) {
        _uiState.value = _uiState.value.copy(petId = id)
    }

    fun alternarServico(servico: String) {
        val atuais = _uiState.value.servicosSelecionados
        val novos = if (atuais.contains(servico)) atuais - servico else atuais + servico
        _uiState.value = _uiState.value.copy(servicosSelecionados = novos)
    }

    fun alterarData(texto: String) {
        _uiState.value = _uiState.value.copy(data = texto.filter { it.isDigit() }.take(8))
    }

    fun alterarHorario(texto: String) {
        _uiState.value = _uiState.value.copy(horario = texto.filter { it.isDigit() }.take(4))
    }

    fun alterarObs(texto: String) {
        _uiState.value = _uiState.value.copy(obs = texto)
    }

    fun confirmarData(millis: Long?) {
        if (millis == null) return
        val formatador = SimpleDateFormat("ddMMyyyy", Locale.forLanguageTag("pt-BR"))
        formatador.timeZone = TimeZone.getTimeZone("UTC")
        _uiState.value = _uiState.value.copy(data = formatador.format(Date(millis)))
    }

    fun confirmarHorario(hora: Int, minuto: Int) {
        val texto = String.format(Locale.forLanguageTag("pt-BR"), "%02d%02d", hora, minuto)
        _uiState.value = _uiState.value.copy(horario = texto)
    }

    fun agendar(petshopId: Int, onAgendado: () -> Unit) {
        val e = _uiState.value
        val pet = e.petAtual()

        if (pet == null) {
            _uiState.value = e.copy(erro = "Escolha um pet.")
            return
        }
        if (e.servicosSelecionados.isEmpty()) {
            _uiState.value = e.copy(erro = "Escolha pelo menos um serviço.")
            return
        }
        if (e.data.length != 8) {
            _uiState.value = e.copy(erro = "Data inválida.")
            return
        }
        if (e.horario.length != 4) {
            _uiState.value = e.copy(erro = "Horário inválido.")
            return
        }

        val data =
            e.data.substring(0, 2) + "/" + e.data.substring(2, 4) + "/" + e.data.substring(4, 8)
        val horario = e.horario.substring(0, 2) + ":" + e.horario.substring(2, 4)

        val formatador = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
        formatador.isLenient = false
        val dataEscolhida = try {
            formatador.parse("$data $horario")
        } catch (ex: Exception) {
            null
        }
        if (dataEscolhida == null) {
            _uiState.value = e.copy(erro = "Data ou horário inexistente.")
            return
        }
        if (dataEscolhida.before(Calendar.getInstance().time)) {
            _uiState.value = e.copy(erro = "Horário já passou!")
            return
        }

        viewModelScope.launch {
            val novo = Agendamento(
                petId = pet.id,
                petshopId = petshopId,
                servico = e.servicosSelecionados.joinToString(", "),
                data = data,
                horario = horario,
                observacoes = e.obs.trim()
            )
            agendamentoRepository.inserir(novo)
            _uiState.value = _uiState.value.copy(
                data = "",
                horario = "",
                obs = "",
                erro = "",
                servicosSelecionados = emptySet()
            )
            onAgendado()
        }
    }

    fun alternarConcluido(agendamento: Agendamento) {
        viewModelScope.launch {
            agendamentoRepository.atualizar(agendamento.copy(concluido = !agendamento.concluido))
        }
    }

    fun remover(agendamento: Agendamento) {
        viewModelScope.launch {
            agendamentoRepository.deletar(agendamento)
        }
    }
}
