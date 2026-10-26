package com.example.petapp.data.repository

import com.example.petapp.data.Agendamento
import com.example.petapp.data.AgendamentoDao
import kotlinx.coroutines.flow.Flow

class AgendamentoRepository(private val dao: AgendamentoDao) {

    fun buscarTodos(): Flow<List<Agendamento>> = dao.buscarTodos()

    suspend fun buscarPorId(id: Int): Agendamento? = dao.buscarPorId(id)

    suspend fun inserir(agendamento: Agendamento) = dao.inserir(agendamento)

    suspend fun atualizar(agendamento: Agendamento) = dao.atualizar(agendamento)

    suspend fun deletar(agendamento: Agendamento) = dao.deletar(agendamento)

    suspend fun deletarTodosDoPet(petId: Int) {
        for (agendamento in dao.buscarPorPetId(petId)) {
            dao.deletar(agendamento)
        }
    }
}
