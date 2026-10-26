package com.example.petapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val cpf: String,
    val email: String,
    val celular: String,
    val senha: String,
    val fotoUri: String = "",
    val cep: String = "",
    val endereco: String = ""
)

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val especie: String,
    val sexo: String,
    val raca: String,
    val idadeAnos: Int,
    val pesoKg: Double,
    val fotoUri: String = ""
)

@Entity(tableName = "petshops")
data class Petshop(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val profissionais: String,
    val servicosText: String,
    val distanciaMock: String,
    val avaliacao: Double
)

@Entity(tableName = "agendamentos")
data class Agendamento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val petId: Int,
    val petshopId: Int,
    val servico: String,
    val data: String,
    val horario: String,
    val observacoes: String = "",
    val concluido: Boolean = false
)
