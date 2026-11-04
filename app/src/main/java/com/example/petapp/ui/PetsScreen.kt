@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petapp.data.Pet
import com.example.petapp.ui.theme.FundoBege
import com.example.petapp.ui.theme.VerdePrincipal
import com.example.petapp.ui.theme.VermelhoAlerta
import com.example.petapp.viewmodel.PetsViewModel

@Composable
fun PetsScreen(
    fabrica: ViewModelProvider.Factory,
    onPetClick: (Int) -> Unit,
    vm: PetsViewModel = viewModel(factory = fabrica)
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            vm.alterarFoto(it.toString())
        }
    }

    estado.petParaRemover?.let { pet ->
        AlertDialog(
            onDismissRequest = { vm.cancelarRemocao() },
            title = { Text("Remover ${pet.nome}?") },
            text = { Text("Os agendamentos deste pet também serão removidos.") },
            confirmButton = { TextButton(onClick = { vm.confirmarRemocao() }) { Text("Remover", color = VermelhoAlerta) } },
            dismissButton = { TextButton(onClick = { vm.cancelarRemocao() }) { Text("Cancelar") } }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(FundoBege), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Meus pets", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Adicionar pet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black, modifier = Modifier.align(Alignment.Start))

                    Text("Toque na imagem para importar da galeria", fontSize = 12.sp, color = Color.Gray)
                    Box(modifier = Modifier.clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        FotoPerfil(uri = estado.fotoSelecionada, modifier = Modifier.size(80.dp))
                    }

                    OutlinedTextField(value = estado.nome, onValueChange = { vm.alterarNome(it) }, label = { Text("Nome") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = estado.raca, onValueChange = { vm.alterarRaca(it) }, label = { Text("Raça (opcional)") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = estado.especie == "Cão", onClick = { vm.selecionarEspecie("Cão") }, label = { Text("Cão") })
                        FilterChip(selected = estado.especie == "Gato", onClick = { vm.selecionarEspecie("Gato") }, label = { Text("Gato") })
                    }

                    if (estado.especie.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = estado.sexo == "Macho", onClick = { vm.selecionarSexo("Macho") }, label = { Text("Macho") }, leadingIcon = { Icon(Icons.Rounded.Male, contentDescription = null) })
                            FilterChip(selected = estado.sexo == "Fêmea", onClick = { vm.selecionarSexo("Fêmea") }, label = { Text("Fêmea") }, leadingIcon = { Icon(Icons.Rounded.Female, contentDescription = null) })
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = estado.idade, onValueChange = { vm.alterarIdade(it) }, label = { Text("Idade (anos)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = estado.peso, onValueChange = { vm.alterarPeso(it) }, label = { Text("Peso (kg)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.weight(1f))
                    }
                    if (estado.erro.isNotEmpty()) Text(estado.erro, color = VermelhoAlerta, fontSize = 12.sp)

                    Button(
                        onClick = { vm.adicionarPet() },
                        modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(14.dp)
                    ) { Text("Adicionar pet", fontWeight = FontWeight.Bold) }
                }
            }
        }
        item { Text("Pets cadastrados (${estado.pets.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black) }
        if (estado.pets.isEmpty()) item { Text("Nenhum pet cadastrado ainda.", color = Color.Gray) }
        items(estado.pets, key = { it.id }) { pet ->
            PetCard(pet = pet, pendentes = estado.pendentesDoPet(pet.id), onClick = { onPetClick(pet.id) }, onRemover = { vm.pedirRemocao(pet) })
        }
    }
}

@Composable
fun PetCard(pet: Pet, pendentes: Int, onClick: () -> Unit, onRemover: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            FotoPerfil(uri = pet.fotoUri, modifier = Modifier.size(52.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("${pet.sexo} • ${pet.raca} • ${pet.idadeAnos} ano(s)", color = Color.Gray, fontSize = 12.sp)
                Text(if (pendentes == 0) "Sem agendamentos pendentes" else "$pendentes pendente(s)", color = VerdePrincipal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onRemover) { Icon(Icons.Default.Delete, contentDescription = "Remover", tint = VermelhoAlerta) }
        }
    }
}
