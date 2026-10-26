package com.example.petapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val emailOuCpf: String = "",
    val senha: String = "",
    val lembrarMim: Boolean = true,
    val erro: String = "",
    val emailRecuperacao: String = "",
    val novaSenhaRecuperacao: String = ""
)

class LoginViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun alterarEmailOuCpf(texto: String) {
        if (texto.all { it.isDigit() } && texto.length > 11) return
        _uiState.value = _uiState.value.copy(emailOuCpf = texto)
    }

    fun alterarSenha(texto: String) {
        _uiState.value = _uiState.value.copy(senha = texto)
    }

    fun alterarLembrarMim(valor: Boolean) {
        _uiState.value = _uiState.value.copy(lembrarMim = valor)
    }

    fun alterarEmailRecuperacao(texto: String) {
        _uiState.value = _uiState.value.copy(emailRecuperacao = texto)
    }

    fun alterarNovaSenha(texto: String) {
        _uiState.value = _uiState.value.copy(novaSenhaRecuperacao = texto)
    }

    fun entrar(onSucesso: () -> Unit) {
        val estado = _uiState.value
        if (estado.emailOuCpf.isBlank() || estado.senha.isBlank()) {
            _uiState.value = estado.copy(erro = "Preencha o e-mail/CPF e a senha.")
            return
        }
        viewModelScope.launch {
            val usuario = usuarioRepository.autenticar(estado.emailOuCpf.trim(), estado.senha)
            if (usuario != null) {
                usuarioRepository.iniciarSessao(usuario.id, estado.lembrarMim)
                _uiState.value = _uiState.value.copy(erro = "")
                onSucesso()
            } else {
                _uiState.value = _uiState.value.copy(erro = "E-mail/CPF ou senha incorretos.")
            }
        }
    }

    fun salvarNovaSenha(mostrarMensagem: (String) -> Unit, onSalvo: () -> Unit) {
        val estado = _uiState.value
        if (!estado.emailRecuperacao.contains("@")) {
            mostrarMensagem("Digite um e-mail válido")
            return
        }
        if (estado.novaSenhaRecuperacao.length < 6) {
            mostrarMensagem("A nova senha deve ter pelo menos 6 caracteres")
            return
        }
        viewModelScope.launch {
            val usuario = usuarioRepository.buscarPorEmailOuCpf(estado.emailRecuperacao.trim(), "")
            if (usuario != null) {
                usuarioRepository.atualizar(usuario.copy(senha = estado.novaSenhaRecuperacao))
                _uiState.value =
                    _uiState.value.copy(emailRecuperacao = "", novaSenhaRecuperacao = "")
                mostrarMensagem("Senha atualizada no banco de dados!")
                onSalvo()
            } else {
                mostrarMensagem("E-mail não encontrado.")
            }
        }
    }
}
