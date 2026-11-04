package com.example.petapp.data

import java.util.Locale


object Servicos {
    val precosBase: Map<String, Double> = linkedMapOf(
        "Banho" to 59.0, "Tosa" to 75.0, "Vacinas" to 89.0,
        "Consulta" to 129.0, "Hotel" to 110.0, "Táxi pet" to 25.0
    )
}

fun Pet.porte(): String = when {
    pesoKg <= 10.0 -> "Pequeno"
    pesoKg <= 25.0 -> "Médio"
    else -> "Grande"
}

fun Pet.fatorPreco(): Double = when (porte()) {
    "Pequeno" -> 1.0
    "Médio" -> 1.25
    else -> 1.5
}

fun Pet.idadeHumana(): Int = when {
    idadeAnos <= 0 -> 0
    idadeAnos == 1 -> 15
    idadeAnos == 2 -> 24
    else -> 24 + (idadeAnos - 2) * (if (especie == "Gato") 4 else 5)
}

fun Pet.precoDoServico(servico: String): Double = (Servicos.precosBase[servico] ?: 0.0) * fatorPreco()

fun Double.comoReais(): String = "R$ " + String.format(Locale.forLanguageTag("pt-BR"), "%.2f", this)

fun Double.comoKg(): String = if (this % 1.0 == 0.0) "${toInt()} kg" else "$this kg".replace('.', ',')
