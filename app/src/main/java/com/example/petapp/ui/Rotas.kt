package com.example.petapp.ui

import android.net.Uri

object Rotas {
    const val LOGIN = "login"
    const val CADASTRO = "cadastro"
    const val INICIO = "inicio"
    const val PETS = "pets"
    const val CONTA = "conta"
    const val EDITAR_PERFIL = "editar_perfil"

    const val AGENDA = "agenda?servico={servico}&petId={petId}"

    const val DETALHE_PET = "pet/{petId}"
    const val DETALHE_AGENDAMENTO = "agendamento/{agendamentoId}"

    fun agenda(servico: String = "", petId: Int = -1) = "agenda?servico=${Uri.encode(servico)}&petId=$petId"
    fun detalhePet(id: Int) = "pet/$id"
    fun detalheAgendamento(id: Int) = "agendamento/$id"
}
