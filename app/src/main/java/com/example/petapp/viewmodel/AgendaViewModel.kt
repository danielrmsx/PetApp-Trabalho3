package com.example.petapp.viewmodel

import android.util.Log
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
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
    val erro: String = "",
    // Controles da tela
    val abaSelecionada: Int = 0,
    val petshopExpandido: Int = -1,
    val mostrarDatePicker: Boolean = false,
    val mostrarTimePicker: Boolean = false
) {
    val petAtual: Pet? get() = pets.find { it.id == petId } ?: pets.firstOrNull()
    val agendamentosPendentes: List<Agendamento> get() = agendamentos.filter { !it.concluido }
    val agendamentosConcluidos: List<Agendamento> get() = agendamentos.filter { it.concluido }
    val petshopsFiltrados: List<Petshop>
        get() = if (pesquisaServico.isBlank()) petshops else petshops.filter { it.servicosText.contains(pesquisaServico, ignoreCase = true) }
    fun nomeDoPet(id: Int): String = pets.find { it.id == id }?.nome ?: "Pet"
    fun nomeDoPetshop(id: Int): String = petshops.find { it.id == id }?.nome ?: "Petshop"
}

private data class DadosAgenda(val usuario: Usuario?, val pets: List<Pet>, val petshops: List<Petshop>, val agendamentos: List<Agendamento>)

class AgendaViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val petRepository: PetRepository,
    private val petshopRepository: PetshopRepository,
    private val agendamentoRepository: AgendamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    private var iniciado = false
    private var petPreSelecionado = -1
    private var cepJaPreenchido = false

    init {
        viewModelScope.launch {
            try {
                petshopRepository.garantirPetshopsIniciais()
            } catch (e: Exception) {
                Log.e("AgendaViewModel", "Erro ao criar petshops", e)
            }
        }
        viewModelScope.launch {
            combine(
                usuarioRepository.observarUsuarioLogado(),
                petRepository.buscarTodos(),
                petshopRepository.buscarTodos(),
                agendamentoRepository.buscarTodos()
            ) { usuario, pets, petshops, ags -> DadosAgenda(usuario, pets, petshops, ags) }
                .catch { e -> Log.e("AgendaViewModel", "Erro ao carregar", e) }
                .collect { dados ->
                    _uiState.update { atual ->
                        val novoPetId = if (atual.petId == -1) {
                            if (petPreSelecionado > 0) petPreSelecionado else dados.pets.firstOrNull()?.id ?: -1
                        } else atual.petId
                        // Preenche o CEP com o do perfil só uma vez.
                        val cepUsuario = dados.usuario?.cep ?: ""
                        val novoCep = if (!cepJaPreenchido && atual.cepBusca.isEmpty() && cepUsuario.isNotBlank()) {
                            cepJaPreenchido = true
                            cepUsuario
                        } else atual.cepBusca
                        atual.copy(usuario = dados.usuario, pets = dados.pets, petshops = dados.petshops, agendamentos = dados.agendamentos, petId = novoPetId, cepBusca = novoCep)
                    }
                }
        }
    }

    fun iniciar(servicoInicial: String, petInicial: Int) {
        if (iniciado) return
        iniciado = true
        petPreSelecionado = petInicial
        _uiState.update {
            it.copy(
                servicosSelecionados = if (servicoInicial.isNotBlank()) setOf(servicoInicial) else setOf("Banho"),
                petId = if (petInicial > 0) petInicial else it.petId
            )
        }
    }

    fun alterarCep(texto: String) {
        val numeros = texto.filter { it.isDigit() }
        if (numeros.length <= 8) _uiState.update { it.copy(cepBusca = numeros) }
    }
    fun alterarPesquisa(texto: String) = _uiState.update { it.copy(pesquisaServico = texto) }
    fun selecionarAba(indice: Int) = _uiState.update { it.copy(abaSelecionada = indice, petshopExpandido = -1) }
    fun alternarPetshop(id: Int) = _uiState.update { it.copy(petshopExpandido = if (it.petshopExpandido == id) -1 else id) }
    fun selecionarPet(id: Int) = _uiState.update { it.copy(petId = id) }
    fun alternarServico(servico: String) = _uiState.update {
        val atuais = it.servicosSelecionados
        it.copy(servicosSelecionados = if (atuais.contains(servico)) atuais - servico else atuais + servico)
    }
    fun alterarData(texto: String) = _uiState.update { it.copy(data = texto.filter { c -> c.isDigit() }.take(8)) }
    fun alterarHorario(texto: String) = _uiState.update { it.copy(horario = texto.filter { c -> c.isDigit() }.take(4)) }
    fun alterarObs(texto: String) = _uiState.update { it.copy(obs = texto) }

    fun abrirDatePicker() = _uiState.update { it.copy(mostrarDatePicker = true) }
    fun fecharDatePicker() = _uiState.update { it.copy(mostrarDatePicker = false) }
    fun abrirTimePicker() = _uiState.update { it.copy(mostrarTimePicker = true) }
    fun fecharTimePicker() = _uiState.update { it.copy(mostrarTimePicker = false) }

    fun confirmarData(millis: Long?) {
        if (millis != null) {
            val formatador = SimpleDateFormat("ddMMyyyy", Locale.forLanguageTag("pt-BR"))
            formatador.timeZone = TimeZone.getTimeZone("UTC")
            _uiState.update { it.copy(data = formatador.format(Date(millis))) }
        }
        fecharDatePicker()
    }

    fun confirmarHorario(hora: Int, minuto: Int) {
        _uiState.update { it.copy(horario = String.format(Locale.forLanguageTag("pt-BR"), "%02d%02d", hora, minuto), mostrarTimePicker = false) }
    }

    fun agendar(petshopId: Int) {
        val e = _uiState.value
        val pet = e.petAtual
        val dataFormatada = if (e.data.length == 8) "${e.data.substring(0, 2)}/${e.data.substring(2, 4)}/${e.data.substring(4, 8)}" else e.data
        val horarioFormatado = if (e.horario.length == 4) "${e.horario.substring(0, 2)}:${e.horario.substring(2, 4)}" else e.horario

        val erroBasico = when {
            pet == null -> "Escolha um pet."
            e.servicosSelecionados.isEmpty() -> "Escolha pelo menos um serviço."
            !REGEX_DATA.matches(dataFormatada) -> "Data inválida."
            !REGEX_HORA.matches(horarioFormatado) -> "Horário inválido."
            else -> null
        }
        if (erroBasico != null || pet == null) {
            _uiState.update { it.copy(erro = erroBasico ?: "") }
            return
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
        sdf.isLenient = false
        val dataEscolhida = try {
            sdf.parse("$dataFormatada $horarioFormatado")
        } catch (ex: Exception) {
            _uiState.update { it.copy(erro = "Data ou horário inexistente.") }
            return
        }
        if (dataEscolhida != null && dataEscolhida.before(Calendar.getInstance().time)) {
            _uiState.update { it.copy(erro = "Horário já passou!") }
            return
        }

        viewModelScope.launch {
            try {
                agendamentoRepository.inserir(
                    Agendamento(petId = pet.id, petshopId = petshopId, servico = e.servicosSelecionados.joinToString(", "), data = dataFormatada, horario = horarioFormatado, observacoes = e.obs.trim())
                )
                _uiState.update { it.copy(data = "", horario = "", obs = "", erro = "", petshopExpandido = -1, abaSelecionada = 1, servicosSelecionados = emptySet()) }
            } catch (ex: Exception) {
                Log.e("AgendaViewModel", "Erro ao inserir agendamento", ex)
                _uiState.update { it.copy(erro = "Erro ao salvar o agendamento.") }
            }
        }
    }

    fun alternarConcluido(agendamento: Agendamento) {
        viewModelScope.launch {
            try {
                agendamentoRepository.atualizar(agendamento.copy(concluido = !agendamento.concluido))
            } catch (e: Exception) {
                Log.e("AgendaViewModel", "Erro ao atualizar", e)
            }
        }
    }

    fun remover(agendamento: Agendamento) {
        viewModelScope.launch {
            try {
                agendamentoRepository.deletar(agendamento)
            } catch (e: Exception) {
                Log.e("AgendaViewModel", "Erro ao remover", e)
            }
        }
    }

    private companion object {
        val REGEX_DATA = Regex("""^\d{2}/\d{2}/\d{4}$""")
        val REGEX_HORA = Regex("""^([01]\d|2[0-3]):[0-5]\d$""")
    }
}
