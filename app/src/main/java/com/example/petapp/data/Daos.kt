package com.example.petapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface UsuarioDao {
    @Insert suspend fun inserir(usuario: Usuario): Long
    @Update suspend fun atualizar(usuario: Usuario)
    @Delete suspend fun deletar(usuario: Usuario)

    @Query("SELECT * FROM usuarios")
    fun buscarTodos(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE (LOWER(email) = LOWER(:identificador) OR cpf = :cpfLimpo) AND senha = :senha LIMIT 1")
    suspend fun autenticar(identificador: String, cpfLimpo: String, senha: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) OR cpf = :cpf LIMIT 1")
    suspend fun buscarPorEmailOuCpf(email: String, cpf: String): Usuario?


    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<Usuario?>
}

@Dao
interface PetDao {
    @Insert suspend fun inserir(pet: Pet): Long
    @Update suspend fun atualizar(pet: Pet)
    @Delete suspend fun deletar(pet: Pet)

    @Query("SELECT * FROM pets ORDER BY id ASC")
    fun buscarTodos(): Flow<List<Pet>>

    @Query("SELECT * FROM pets WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Pet?

    @Query("SELECT * FROM pets WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<Pet?>
}

@Dao
interface PetshopDao {
    @Insert suspend fun inserir(petshop: Petshop): Long
    @Update suspend fun atualizar(petshop: Petshop)
    @Delete suspend fun deletar(petshop: Petshop)

    @Query("SELECT * FROM petshops")
    fun buscarTodos(): Flow<List<Petshop>>

    @Query("SELECT * FROM petshops WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Petshop?

    @Query("SELECT COUNT(*) FROM petshops")
    suspend fun contar(): Int
}

@Dao
interface AgendamentoDao {
    @Insert suspend fun inserir(agendamento: Agendamento): Long
    @Update suspend fun atualizar(agendamento: Agendamento)
    @Delete suspend fun deletar(agendamento: Agendamento)

    @Query("SELECT * FROM agendamentos ORDER BY id ASC")
    fun buscarTodos(): Flow<List<Agendamento>>

    @Query("SELECT * FROM agendamentos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Agendamento?

    @Query("SELECT * FROM agendamentos WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<Agendamento?>

    @Query("SELECT * FROM agendamentos WHERE petId = :petId")
    suspend fun buscarPorPetId(petId: Int): List<Agendamento>

    @Query("SELECT * FROM agendamentos WHERE petId = :petId ORDER BY id ASC")
    fun observarPorPetId(petId: Int): Flow<List<Agendamento>>
}
