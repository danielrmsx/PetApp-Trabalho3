package com.example.petapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petapp.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val emailOuCpf: String = "",
    val senha: String = "",
    val lembrarMim: Boolean = true,
    val erro: String = "",
    val mostrarEsqueciSenha: Boolean = false,
    val emailRecuperacao: String = "",
    val novaSenhaRecuperacao: String = "",
    val mensagem: String? = null,
    val loginRealizado: Boolean = false
)

class LoginViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun alterarEmailOuCpf(texto: String) {
        if (texto.all { it.isDigit() } && texto.length > 11) return
        _uiState.update { it.copy(emailOuCpf = texto) }
    }

    fun alterarSenha(texto: String) = _uiState.update { it.copy(senha = texto) }
    fun alterarLembrarMim(valor: Boolean) = _uiState.update { it.copy(lembrarMim = valor) }

    fun abrirEsqueciSenha() = _uiState.update { it.copy(mostrarEsqueciSenha = true) }
    fun fecharEsqueciSenha() = _uiState.update { it.copy(mostrarEsqueciSenha = false) }
    fun alterarEmailRecuperacao(texto: String) = _uiState.update { it.copy(emailRecuperacao = texto) }
    fun alterarNovaSenha(texto: String) = _uiState.update { it.copy(novaSenhaRecuperacao = texto) }

    fun entrar() {
        val estado = _uiState.value
        if (estado.emailOuCpf.isBlank() || estado.senha.isBlank()) {
            _uiState.update { it.copy(erro = "Preencha o e-mail/CPF e a senha.") }
            return
        }
        viewModelScope.launch {
            try {
                val usuario = usuarioRepository.autenticar(estado.emailOuCpf.trim(), estado.senha)
                if (usuario != null) {
                    usuarioRepository.iniciarSessao(usuario.id, estado.lembrarMim)
                    _uiState.update { it.copy(erro = "", loginRealizado = true) }
                } else {
                    _uiState.update { it.copy(erro = "E-mail/CPF ou senha incorretos.") }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Erro ao fazer login", e)
                _uiState.update { it.copy(erro = "Erro ao acessar o banco de dados.") }
            }
        }
    }

    fun salvarNovaSenha() {
        val estado = _uiState.value
        when {
            !estado.emailRecuperacao.contains("@") ->
                _uiState.update { it.copy(mensagem = "Digite um e-mail válido") }
            estado.novaSenhaRecuperacao.length < 6 ->
                _uiState.update { it.copy(mensagem = "A nova senha deve ter pelo menos 6 caracteres") }
            else -> viewModelScope.launch {
                try {
                    val usuario = usuarioRepository.buscarPorEmailOuCpf(estado.emailRecuperacao.trim(), "")
                    if (usuario != null) {
                        usuarioRepository.atualizar(usuario.copy(senha = estado.novaSenhaRecuperacao))
                        _uiState.update {
                            it.copy(
                                mensagem = "Senha atualizada no banco de dados!",
                                emailRecuperacao = "", novaSenhaRecuperacao = "", mostrarEsqueciSenha = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(mensagem = "E-mail não encontrado.") }
                    }
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Erro ao redefinir senha", e)
                }
            }
        }
    }

    fun mensagemExibida() = _uiState.update { it.copy(mensagem = null) }

    fun navegacaoConcluida() = _uiState.update { it.copy(loginRealizado = false) }
}
