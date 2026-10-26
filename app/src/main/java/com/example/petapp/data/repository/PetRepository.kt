package com.example.petapp.data.repository

import com.example.petapp.data.Pet
import com.example.petapp.data.PetDao
import kotlinx.coroutines.flow.Flow

class PetRepository(private val dao: PetDao) {

    fun buscarTodos(): Flow<List<Pet>> = dao.buscarTodos()

    suspend fun buscarPorId(id: Int): Pet? = dao.buscarPorId(id)

    suspend fun inserir(pet: Pet) = dao.inserir(pet)

    suspend fun atualizar(pet: Pet) = dao.atualizar(pet)

    suspend fun deletar(pet: Pet) = dao.deletar(pet)
}
