package com.example.petapp.data.repository

import android.content.SharedPreferences
import com.example.petapp.data.Usuario
import com.example.petapp.data.UsuarioDao
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val dao: UsuarioDao, private val prefs: SharedPreferences) {

    fun buscarTodos(): Flow<List<Usuario>> = dao.buscarTodos()

    suspend fun buscarPorId(id: Int): Usuario? = dao.buscarPorId(id)

    suspend fun buscarPorEmailOuCpf(email: String, cpf: String): Usuario? = dao.buscarPorEmailOuCpf(email, cpf)

    suspend fun inserir(usuario: Usuario) = dao.inserir(usuario)

    suspend fun atualizar(usuario: Usuario) = dao.atualizar(usuario)

    suspend fun autenticar(identificador: String, senha: String): Usuario? {
        val cpfLimpo = identificador.filter { it.isDigit() }.ifEmpty { identificador }
        return dao.autenticar(identificador, cpfLimpo, senha)
    }

    fun iniciarSessao(id: Int, lembrar: Boolean) {
        idLogado = id
        if (lembrar) {
            prefs.edit().putInt("usuario_logado_id", id).apply()
        } else {
            prefs.edit().remove("usuario_logado_id").apply()
        }
    }

    fun idUsuarioLogado(): Int {
        return if (idLogado != -1) idLogado else prefs.getInt("usuario_logado_id", -1)
    }

    fun encerrarSessao() {
        idLogado = -1
        prefs.edit().remove("usuario_logado_id").apply()
    }

    companion object {
        private var idLogado = -1
    }
}
