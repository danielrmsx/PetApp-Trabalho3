package com.example.petapp.data.repository

import com.example.petapp.data.Petshop
import com.example.petapp.data.PetshopDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PetshopRepository(private val dao: PetshopDao) {

    fun buscarTodos(): Flow<List<Petshop>> = dao.buscarTodos()

    suspend fun buscarPorId(id: Int): Petshop? = withContext(Dispatchers.IO) { dao.buscarPorId(id) }

    suspend fun inserir(petshop: Petshop): Long = withContext(Dispatchers.IO) { dao.inserir(petshop) }

    suspend fun garantirPetshopsIniciais() = withContext(Dispatchers.IO) {
        if (dao.contar() == 0) {
            dao.inserir(Petshop(nome = "Petz Centro", profissionais = "Ana, João", servicosText = "Banho, Tosa, Táxi pet", distanciaMock = "1.2 km", avaliacao = 4.8))
            dao.inserir(Petshop(nome = "Cãopeão Batel", profissionais = "Dr. Marcos", servicosText = "Consulta, Vacinas", distanciaMock = "3.5 km", avaliacao = 4.9))
            dao.inserir(Petshop(nome = "Juvevê Pets", profissionais = "Julia, Roberto", servicosText = "Banho, Tosa, Hotel", distanciaMock = "4.1 km", avaliacao = 4.5))
            dao.inserir(Petshop(nome = "Estética Animal VIP", profissionais = "Carla", servicosText = "Banho, Tosa", distanciaMock = "2.0 km", avaliacao = 4.6))
            dao.inserir(Petshop(nome = "Clínica Vida Pet", profissionais = "Dra. Fernanda, Dr. Lucas", servicosText = "Consulta, Vacinas, Cirurgias", distanciaMock = "5.5 km", avaliacao = 4.9))
        }
    }
}
