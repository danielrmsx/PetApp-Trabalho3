package com.example.petapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Usuario
import com.example.petapp.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditarPerfilUiState(
    val usuario: Usuario? = null,
    val cpf: String = "",
    val nome: String = "",
    val celular: String = "",
    val cep: String = "",
    val endereco: String = "",
    val fotoUri: String = "",
    val mensagem: String? = null,
    val salvo: Boolean = false
)

class EditarPerfilViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val u = usuarioRepository.buscarPorId(usuarioRepository.idUsuarioLogado())
                if (u != null) {
                    _uiState.update {
                        it.copy(usuario = u, cpf = u.cpf, nome = u.nome, celular = u.celular, cep = u.cep, endereco = u.endereco, fotoUri = u.fotoUri)
                    }
                }
            } catch (e: Exception) {
                Log.e("EditarPerfilViewModel", "Erro ao carregar perfil", e)
            }
        }
    }

    fun alterarNome(texto: String) = _uiState.update { it.copy(nome = texto) }
    fun alterarCelular(texto: String) = _uiState.update { it.copy(celular = texto.filter { c -> c.isDigit() }.take(11)) }
    fun alterarCep(texto: String) = _uiState.update { it.copy(cep = texto.filter { c -> c.isDigit() }.take(8)) }
    fun alterarEndereco(texto: String) = _uiState.update { it.copy(endereco = texto) }
    fun alterarFoto(uri: String) = _uiState.update { it.copy(fotoUri = uri) }

    fun salvar() {
        val e = _uiState.value
        val usuario = e.usuario ?: return
        if (e.nome.isBlank()) {
            _uiState.update { it.copy(mensagem = "O nome não pode ficar vazio.") }
            return
        }
        viewModelScope.launch {
            try {
                usuarioRepository.atualizar(
                    usuario.copy(nome = e.nome, celular = e.celular, cep = e.cep, endereco = e.endereco, fotoUri = e.fotoUri)
                )
                _uiState.update { it.copy(mensagem = "Dados salvos no banco!", salvo = true) }
            } catch (ex: Exception) {
                Log.e("EditarPerfilViewModel", "Erro ao salvar perfil", ex)
                _uiState.update { it.copy(mensagem = "Erro ao salvar os dados.") }
            }
        }
    }

    fun mensagemExibida() = _uiState.update { it.copy(mensagem = null) }
    fun navegacaoConcluida() = _uiState.update { it.copy(salvo = false) }
}
