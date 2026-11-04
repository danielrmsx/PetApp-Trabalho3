@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petapp.data.Agendamento
import com.example.petapp.data.Servicos
import com.example.petapp.ui.theme.FundoBege
import com.example.petapp.ui.theme.VerdePrincipal
import com.example.petapp.ui.theme.VermelhoAlerta
import com.example.petapp.viewmodel.AgendaViewModel
import java.util.Calendar
import java.util.TimeZone

@Composable
fun AgendaScreen(
    fabrica: ViewModelProvider.Factory,
    servicoInicial: String,
    petInicial: Int,
    onAgendamentoClick: (Int) -> Unit,
    vm: AgendaViewModel = viewModel(factory = fabrica)
) {

    LaunchedEffect(Unit) { vm.iniciar(servicoInicial, petInicial) }
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val titulosAbas = listOf("Agendar", "Agendados", "Realizados")

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calHoje = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                return utcTimeMillis >= calHoje.timeInMillis
            }
        }
    )
    val calAtual = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(initialHour = calAtual.get(Calendar.HOUR_OF_DAY), initialMinute = calAtual.get(Calendar.MINUTE), is24Hour = true)

    if (estado.mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { vm.fecharDatePicker() },
            confirmButton = { TextButton(onClick = { vm.confirmarData(datePickerState.selectedDateMillis) }) { Text("Confirmar", color = VerdePrincipal) } },
            dismissButton = { TextButton(onClick = { vm.fecharDatePicker() }) { Text("Cancelar", color = Color.Gray) } }
        ) { DatePicker(state = datePickerState) }
    }
    if (estado.mostrarTimePicker) {
        AlertDialog(
            onDismissRequest = { vm.fecharTimePicker() },
            confirmButton = { TextButton(onClick = { vm.confirmarHorario(timePickerState.hour, timePickerState.minute) }) { Text("Confirmar", color = VerdePrincipal) } },
            dismissButton = { TextButton(onClick = { vm.fecharTimePicker() }) { Text("Cancelar", color = Color.Gray) } },
            text = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { TimePicker(state = timePickerState) } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoBege)) {
        Column(modifier = Modifier.background(Color.White).padding(top = 24.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                titulosAbas.forEachIndexed { index, titulo ->
                    val selecionado = estado.abaSelecionada == index
                    Column(
                        modifier = Modifier.weight(1f).clickable { vm.selecionarAba(index) }.padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = titulo, fontWeight = FontWeight.Bold, color = if (selecionado) VerdePrincipal else Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(if (selecionado) VerdePrincipal else Color.Transparent))
                    }
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (estado.abaSelecionada) {
                0 -> {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = estado.cepBusca,
                                onValueChange = { vm.alterarCep(it) },
                                placeholder = { Text("Informar CEP", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Rounded.Place, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = CepVisualTransformation(),
                                shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(0.45f).height(50.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray, focusedBorderColor = VerdePrincipal, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                            )
                            OutlinedTextField(
                                value = estado.pesquisaServico, onValueChange = { vm.alterarPesquisa(it) },
                                placeholder = { Text("O que seu pet precisa?", fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                                singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(0.55f).height(50.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray, focusedBorderColor = VerdePrincipal, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Petshops perto de si", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    }

                    items(estado.petshopsFiltrados, key = { it.id }) { ps ->
                        val expandido = estado.petshopExpandido == ps.id
                        Card(modifier = Modifier.fillMaxWidth().clickable { vm.alternarPetshop(ps.id) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(ps.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${ps.avaliacao}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("A ${ps.distanciaMock} da sua localização", color = VerdePrincipal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Profissionais: ${ps.profissionais}", color = Color.DarkGray, fontSize = 13.sp)
                                Text("Serviços: ${ps.servicosText}", color = Color.Gray, fontSize = 12.sp)

                                if (expandido) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = FundoBege)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (estado.pets.isEmpty()) {
                                        Text("Cadastre um pet na aba Pets antes de agendar.", color = Color.Gray, fontSize = 13.sp)
                                    } else {
                                        Text("Pet", fontSize = 12.sp, color = Color.Gray)
                                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            estado.pets.forEach { p -> FilterChip(selected = estado.petAtual?.id == p.id, onClick = { vm.selecionarPet(p.id) }, label = { Text(p.nome) }) }
                                        }

                                        Text("Serviços", fontSize = 12.sp, color = Color.Gray)
                                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Servicos.precosBase.keys.forEach { s ->
                                                FilterChip(
                                                    selected = estado.servicosSelecionados.contains(s),
                                                    onClick = { vm.alternarServico(s) },
                                                    label = { Text(s) },
                                                    leadingIcon = { Icon(iconeServico(s), contentDescription = null, modifier = Modifier.size(18.dp)) }
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(value = estado.data, onValueChange = { vm.alterarData(it) }, label = { Text("Data") }, placeholder = { Text("dd/mm/aaaa") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = DataVisualTransformation(), trailingIcon = { IconButton(onClick = { vm.abrirDatePicker() }) { Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = VerdePrincipal) } }, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.weight(1f))
                                            OutlinedTextField(value = estado.horario, onValueChange = { vm.alterarHorario(it) }, label = { Text("Horário") }, placeholder = { Text("hh:mm") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = HoraVisualTransformation(), trailingIcon = { IconButton(onClick = { vm.abrirTimePicker() }) { Icon(Icons.Rounded.Schedule, contentDescription = null, tint = VerdePrincipal) } }, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.weight(1f))
                                        }

                                        OutlinedTextField(value = estado.obs, onValueChange = { vm.alterarObs(it) }, label = { Text("Observações (opcional)") }, minLines = 2, maxLines = 4, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())
                                        if (estado.erro.isNotEmpty()) Text(estado.erro, color = VermelhoAlerta, fontSize = 12.sp)

                                        Button(
                                            onClick = { vm.agendar(ps.id) },
                                            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(14.dp)
                                        ) { Text("Confirmar Agendamento", fontWeight = FontWeight.Bold) }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (estado.agendamentosPendentes.isEmpty()) item { Text("Não tem serviços agendados.", color = Color.Gray) }
                    items(estado.agendamentosPendentes, key = { it.id }) { ag ->
                        AgendamentoCard(agendamento = ag, nomePet = estado.nomeDoPet(ag.petId), nomePetshop = estado.nomeDoPetshop(ag.petshopId), onClick = { onAgendamentoClick(ag.id) }, onConcluir = { vm.alternarConcluido(ag) }, onRemover = { vm.remover(ag) })
                    }
                }
                else -> {
                    if (estado.agendamentosConcluidos.isEmpty()) item { Text("Ainda não tem serviços realizados.", color = Color.Gray) }
                    items(estado.agendamentosConcluidos, key = { it.id }) { ag ->
                        AgendamentoCard(agendamento = ag, nomePet = estado.nomeDoPet(ag.petId), nomePetshop = estado.nomeDoPetshop(ag.petshopId), onClick = { onAgendamentoClick(ag.id) }, onConcluir = { vm.alternarConcluido(ag) }, onRemover = { vm.remover(ag) })
                    }
                }
            }
        }
    }
}

@Composable
fun AgendamentoCard(agendamento: Agendamento, nomePet: String, nomePetshop: String, onClick: () -> Unit, onConcluir: () -> Unit, onRemover: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = agendamento.concluido, onCheckedChange = { onConcluir() }, colors = CheckboxDefaults.colors(checkedColor = VerdePrincipal))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val servicoPrincipal = agendamento.servico.split(",").firstOrNull()?.trim() ?: ""
                    Icon(iconeServico(servicoPrincipal), contentDescription = null, modifier = Modifier.size(16.dp), tint = if (agendamento.concluido) Color.Gray else Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${agendamento.servico} • $nomePet", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (agendamento.concluido) Color.Gray else Color.Black, textDecoration = if (agendamento.concluido) TextDecoration.LineThrough else TextDecoration.None)
                }
                Text("em $nomePetshop", color = VerdePrincipal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("${agendamento.data} às ${agendamento.horario}", color = Color.Gray, fontSize = 12.sp)
            }
            IconButton(onClick = onRemover) { Icon(Icons.Default.Delete, contentDescription = "Remover", tint = VermelhoAlerta) }
        }
    }
}
