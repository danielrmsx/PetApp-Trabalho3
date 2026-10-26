package com.example.petapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.Usuario
import com.example.petapp.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditarPerfilUiState(
    val usuario: Usuario? = null,
    val cpf: String = "",
    val nome: String = "",
    val celular: String = "",
    val cep: String = "",
    val endereco: String = "",
    val fotoUri: String = ""
)

class EditarPerfilViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val u = usuarioRepository.buscarPorId(usuarioRepository.idUsuarioLogado())
            if (u != null) {
                _uiState.value = EditarPerfilUiState(
                    usuario = u, cpf = u.cpf, nome = u.nome, celular = u.celular,
                    cep = u.cep, endereco = u.endereco, fotoUri = u.fotoUri
                )
            }
        }
    }

    fun alterarNome(texto: String) {
        _uiState.value = _uiState.value.copy(nome = texto)
    }

    fun alterarCelular(texto: String) {
        _uiState.value = _uiState.value.copy(celular = texto.filter { it.isDigit() }.take(11))
    }

    fun alterarCep(texto: String) {
        _uiState.value = _uiState.value.copy(cep = texto.filter { it.isDigit() }.take(8))
    }

    fun alterarEndereco(texto: String) {
        _uiState.value = _uiState.value.copy(endereco = texto)
    }

    fun alterarFoto(uri: String) {
        _uiState.value = _uiState.value.copy(fotoUri = uri)
    }

    fun salvar(mostrarMensagem: (String) -> Unit, onSalvo: () -> Unit) {
        val e = _uiState.value
        val usuario = e.usuario ?: return
        if (e.nome.isBlank()) {
            mostrarMensagem("O nome não pode ficar vazio.")
            return
        }
        viewModelScope.launch {
            usuarioRepository.atualizar(
                usuario.copy(nome = e.nome, celular = e.celular, cep = e.cep, endereco = e.endereco, fotoUri = e.fotoUri)
            )
            mostrarMensagem("Dados salvos no banco!")
            onSalvo()
        }
    }
}
