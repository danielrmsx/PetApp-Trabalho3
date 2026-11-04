@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petapp.data.comoKg
import com.example.petapp.data.comoReais
import com.example.petapp.data.idadeHumana
import com.example.petapp.data.porte
import com.example.petapp.ui.theme.FundoBege
import com.example.petapp.ui.theme.VerdeClaro
import com.example.petapp.ui.theme.VerdePrincipal
import com.example.petapp.ui.theme.VermelhoAlerta
import com.example.petapp.viewmodel.DetalhePetViewModel

@Composable
fun DetalhePetScreen(
    fabrica: ViewModelProvider.Factory,
    petId: Int,
    onVoltar: () -> Unit,
    onAgendamentoClick: (Int) -> Unit,
    onAgendarClick: (Int) -> Unit,
    vm: DetalhePetViewModel = viewModel(factory = fabrica)
) {

    LaunchedEffect(petId) { vm.carregar(petId) }
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(FundoBege)) {
        TopAppBar(
            title = { Text(estado.pet?.nome ?: "Carregando...", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = FundoBege)
        )
        val p = estado.pet
        if (p == null) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Buscando pet...", color = Color.Gray)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().background(VerdePrincipal, RoundedCornerShape(24.dp)).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    FotoPerfil(uri = p.fotoUri, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(p.nome, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("${p.sexo} • ${p.raca}", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                }
                CartaoSecao("Informações") {
                    LinhaInfo("Idade", "${p.idadeAnos} ano(s)")
                    LinhaInfo("Peso", p.pesoKg.comoKg())
                    LinhaInfo("Idade em anos humanos", "${p.idadeHumana()} anos")
                    LinhaInfo("Porte", p.porte())
                }
                CartaoSecao("Preços para porte ${p.porte()}") {
                    estado.precos.forEach { (servico, preco) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(iconeServico(servico), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(servico, color = Color.Gray, fontSize = 13.sp)
                            }
                            Text(preco.comoReais(), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text("Valores ajustados pelo porte do pet (pequeno x1,0 • médio x1,25 • grande x1,5).", fontSize = 11.sp, color = Color.Gray)
                }
                CartaoSecao("Atualizar peso") {
                    OutlinedTextField(value = estado.pesoTexto, onValueChange = { vm.alterarPeso(it) }, label = { Text("Peso (kg)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())
                    if (estado.erroPeso.isNotEmpty()) Text(estado.erroPeso, color = VermelhoAlerta, fontSize = 12.sp)
                    Button(onClick = { vm.salvarPeso() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(14.dp)) {
                        Text("Salvar peso no banco", fontWeight = FontWeight.Bold)
                    }
                }
                CartaoSecao("Agendamentos de ${p.nome} (${estado.agendamentosDoPet.size})") {
                    if (estado.agendamentosDoPet.isEmpty()) Text("Nenhum agendamento para este pet.", color = Color.Gray, fontSize = 13.sp)
                    estado.agendamentosDoPet.forEach { ag ->
                        Row(modifier = Modifier.fillMaxWidth().background(VerdeClaro, RoundedCornerShape(12.dp)).clickable { onAgendamentoClick(ag.id) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ag.servico, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("${ag.data} às ${ag.horario}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(if (ag.concluido) "Concluído" else "Pendente", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VerdePrincipal)
                        }
                    }
                    Button(onClick = { onAgendarClick(p.id) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(14.dp)) {
                        Text("Agendar serviço", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
