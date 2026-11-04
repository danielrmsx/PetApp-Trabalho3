package com.example.petapp.data.repository

import com.example.petapp.data.Agendamento
import com.example.petapp.data.AgendamentoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AgendamentoRepository(private val dao: AgendamentoDao) {

    fun buscarTodos(): Flow<List<Agendamento>> = dao.buscarTodos()

    fun observarPorId(id: Int): Flow<Agendamento?> = dao.observarPorId(id)

    fun observarPorPetId(petId: Int): Flow<List<Agendamento>> = dao.observarPorPetId(petId)

    suspend fun buscarPorId(id: Int): Agendamento? = withContext(Dispatchers.IO) { dao.buscarPorId(id) }

    suspend fun inserir(agendamento: Agendamento): Long = withContext(Dispatchers.IO) { dao.inserir(agendamento) }

    suspend fun atualizar(agendamento: Agendamento) = withContext(Dispatchers.IO) { dao.atualizar(agendamento) }

    suspend fun deletar(agendamento: Agendamento) = withContext(Dispatchers.IO) { dao.deletar(agendamento) }

    suspend fun deletarTodosDoPet(petId: Int) = withContext(Dispatchers.IO) {
        dao.buscarPorPetId(petId).forEach { dao.deletar(it) }
    }
}
