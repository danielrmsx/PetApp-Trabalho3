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

data class CadastroUiState(
    val nome: String = "",
    val cpf: String = "",
    val email: String = "",
    val celular: String = "",
    val senha: String = "",
    val confirmarSenha: String = "",
    val aceiteTermos: Boolean = false,
    val erro: String = "",
    val mensagem: String? = null,
    val cadastroConcluido: Boolean = false
)

class CadastroViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CadastroUiState())
    val uiState: StateFlow<CadastroUiState> = _uiState.asStateFlow()

    fun alterarNome(texto: String) = _uiState.update { it.copy(nome = texto) }
    fun alterarCpf(texto: String) {
        val numeros = texto.filter { it.isDigit() }
        if (numeros.length <= 11) _uiState.update { it.copy(cpf = numeros) }
    }
    fun alterarEmail(texto: String) = _uiState.update { it.copy(email = texto) }
    fun alterarCelular(texto: String) {
        val numeros = texto.filter { it.isDigit() }
        if (numeros.length <= 11) _uiState.update { it.copy(celular = numeros) }
    }
    fun alterarSenha(texto: String) = _uiState.update { it.copy(senha = texto) }
    fun alterarConfirmarSenha(texto: String) = _uiState.update { it.copy(confirmarSenha = texto) }
    fun alterarAceiteTermos(valor: Boolean) = _uiState.update { it.copy(aceiteTermos = valor) }

    fun cadastrar() {
        val e = _uiState.value
        val erro = when {
            e.nome.isBlank() -> "Informe seu nome completo."
            e.cpf.length != 11 -> "Informe um CPF válido de 11 dígitos."
            !e.email.contains("@") -> "Informe um e-mail válido."
            e.celular.length < 10 -> "Informe um número de celular válido."
            e.senha.length < 6 -> "A senha deve ter no mínimo 6 caracteres."
            e.senha != e.confirmarSenha -> "As senhas não coincidem."
            else -> null
        }
        if (erro != null) {
            _uiState.update { it.copy(erro = erro) }
            return
        }
        viewModelScope.launch {
            try {
                val existente = usuarioRepository.buscarPorEmailOuCpf(e.email.trim(), e.cpf)
                if (existente != null) {
                    _uiState.update { it.copy(erro = "Já existe uma conta com este e-mail ou CPF.") }
                } else {
                    usuarioRepository.inserir(
                        Usuario(nome = e.nome.trim(), cpf = e.cpf, email = e.email.trim(), celular = e.celular, senha = e.senha, cep = "", endereco = "")
                    )
                    _uiState.update { it.copy(erro = "", mensagem = "Cadastro realizado com sucesso!", cadastroConcluido = true) }
                }
            } catch (ex: Exception) {
                Log.e("CadastroViewModel", "Erro ao cadastrar", ex)
                _uiState.update { it.copy(erro = "Erro ao salvar no banco de dados.") }
            }
        }
    }

    fun mensagemExibida() = _uiState.update { it.copy(mensagem = null) }
    fun navegacaoConcluida() = _uiState.update { it.copy(cadastroConcluido = false) }
}
