package com.example.petapp.data.repository

import android.content.SharedPreferences
import com.example.petapp.data.Usuario
import com.example.petapp.data.UsuarioDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UsuarioRepository(
    private val dao: UsuarioDao,
    private val prefs: SharedPreferences
) {

    private var idLogadoNaSessao: Int = -1

    fun buscarTodos(): Flow<List<Usuario>> = dao.buscarTodos()

    suspend fun inserir(usuario: Usuario): Long = withContext(Dispatchers.IO) { dao.inserir(usuario) }

    suspend fun atualizar(usuario: Usuario) = withContext(Dispatchers.IO) { dao.atualizar(usuario) }

    suspend fun deletar(usuario: Usuario) = withContext(Dispatchers.IO) { dao.deletar(usuario) }

    suspend fun buscarPorId(id: Int): Usuario? = withContext(Dispatchers.IO) { dao.buscarPorId(id) }

    suspend fun buscarPorEmailOuCpf(email: String, cpf: String): Usuario? =
        withContext(Dispatchers.IO) { dao.buscarPorEmailOuCpf(email, cpf) }

    suspend fun autenticar(identificador: String, senha: String): Usuario? = withContext(Dispatchers.IO) {
        val cpfLimpo = identificador.filter { it.isDigit() }.ifEmpty { identificador }
        dao.autenticar(identificador, cpfLimpo, senha)
    }


    fun iniciarSessao(id: Int, lembrar: Boolean) {
        idLogadoNaSessao = id
        if (lembrar) prefs.edit().putInt(CHAVE_USUARIO, id).apply()
        else prefs.edit().remove(CHAVE_USUARIO).apply()
    }

    fun idUsuarioLogado(): Int =
        if (idLogadoNaSessao != -1) idLogadoNaSessao else prefs.getInt(CHAVE_USUARIO, -1)

    fun encerrarSessao() {
        idLogadoNaSessao = -1
        prefs.edit().remove(CHAVE_USUARIO).apply()
    }

    fun observarUsuarioLogado(): Flow<Usuario?> = dao.observarPorId(idUsuarioLogado())

    private companion object {
        const val CHAVE_USUARIO = "usuario_logado_id"
    }
}
