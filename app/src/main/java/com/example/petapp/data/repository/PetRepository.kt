package com.example.petapp.data.repository

import com.example.petapp.data.Pet
import com.example.petapp.data.PetDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PetRepository(private val dao: PetDao) {

    fun buscarTodos(): Flow<List<Pet>> = dao.buscarTodos()

    fun observarPorId(id: Int): Flow<Pet?> = dao.observarPorId(id)

    suspend fun buscarPorId(id: Int): Pet? = withContext(Dispatchers.IO) { dao.buscarPorId(id) }

    suspend fun inserir(pet: Pet): Long = withContext(Dispatchers.IO) { dao.inserir(pet) }

    suspend fun atualizar(pet: Pet) = withContext(Dispatchers.IO) { dao.atualizar(pet) }

    suspend fun deletar(pet: Pet) = withContext(Dispatchers.IO) { dao.deletar(pet) }
}
