@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petapp.data.comoKg
import com.example.petapp.data.comoReais
import com.example.petapp.data.fatorPreco
import com.example.petapp.data.porte
import com.example.petapp.ui.theme.FundoBege
import com.example.petapp.ui.theme.VerdeClaro
import com.example.petapp.ui.theme.VerdePrincipal
import com.example.petapp.ui.theme.VermelhoAlerta
import com.example.petapp.viewmodel.DetalheAgendamentoViewModel

@Composable
fun DetalheAgendamentoScreen(
    fabrica: ViewModelProvider.Factory,
    agendamentoId: Int,
    onVoltar: () -> Unit,
    onPetClick: (Int) -> Unit,
    vm: DetalheAgendamentoViewModel = viewModel(factory = fabrica)
) {
    LaunchedEffect(agendamentoId) { vm.carregar(agendamentoId) }
    val estado by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(estado.excluido) {
        if (estado.excluido) {
            vm.navegacaoConcluida()
            onVoltar()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoBege)) {
        TopAppBar(
            title = { Text("Detalhes do agendamento", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = FundoBege)
        )
        val a = estado.agendamento
        if (a == null) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Buscando agendamento...", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onVoltar, colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)) { Text("Voltar") }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().background(VerdePrincipal, RoundedCornerShape(24.dp)).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(iconeServico(estado.servicoPrincipal), contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(a.servico, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                        Text(if (a.concluido) "Concluído" else "Pendente", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                }

                CartaoSecao("Quando") {
                    LinhaInfo("Data", a.data)
                    LinhaInfo("Horário", a.horario)
                    if (a.observacoes.isNotBlank()) LinhaInfo("Observações", a.observacoes)
                }

                val p = estado.pet
                CartaoSecao("Pet") {
                    if (p == null) {
                        Text("Este pet foi removido.", color = Color.Gray, fontSize = 13.sp)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth().background(VerdeClaro, RoundedCornerShape(12.dp)).clickable { onPetClick(p.id) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            FotoPerfil(uri = p.fotoUri, modifier = Modifier.size(42.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.nome, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("${p.raca} • ${p.pesoKg.comoKg()} • porte ${p.porte()}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ver pet", tint = Color.DarkGray)
                        }
                    }
                }

                if (p != null) {
                    CartaoSecao("Valor estimado") {
                        LinhaInfo("Preço base", estado.precoBaseTotal.comoReais())
                        LinhaInfo("Ajuste por porte (${p.porte()})", "x${p.fatorPreco().toString().replace('.', ',')}")
                        LinhaInfo("Total", estado.totalFinal.comoReais())
                    }
                }

                Button(onClick = { vm.alternarConcluido() }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(16.dp)) {
                    Text(if (a.concluido) "Reabrir agendamento" else "Marcar como concluído", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { vm.cancelarAgendamento() }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                    Text("Cancelar agendamento", color = VermelhoAlerta, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
