@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import com.example.petapp.ui.theme.FundoBege
import com.example.petapp.ui.theme.VerdePrincipal
import com.example.petapp.viewmodel.EditarPerfilViewModel

@Composable
fun EditarPerfilScreen(
    fabrica: ViewModelProvider.Factory,
    onVoltar: () -> Unit,
    vm: EditarPerfilViewModel = viewModel(factory = fabrica)
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            vm.alterarFoto(it.toString())
        }
    }

    LaunchedEffect(estado.mensagem) {
        estado.mensagem?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.mensagemExibida()
        }
    }
    LaunchedEffect(estado.salvo) {
        if (estado.salvo) {
            vm.navegacaoConcluida()
            onVoltar()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoBege)) {
        TopAppBar(
            title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = FundoBege)
        )

        if (estado.usuario != null) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Toque na imagem para alterar a sua foto", fontSize = 12.sp, color = Color.Gray)
                        Box(modifier = Modifier.clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            FotoPerfil(uri = estado.fotoUri, modifier = Modifier.size(80.dp), fallbackIcon = Icons.Default.Person)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(value = estado.cpf, onValueChange = { }, label = { Text("CPF") }, enabled = false, singleLine = true, visualTransformation = CpfVisualTransformation(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.DarkGray, disabledBorderColor = Color.LightGray, disabledLabelColor = Color.Gray), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = estado.nome, onValueChange = { vm.alterarNome(it) }, label = { Text("Nome completo") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = estado.celular, onValueChange = { vm.alterarCelular(it) }, label = { Text("Celular") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = TelefoneVisualTransformation(), shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = estado.cep, onValueChange = { vm.alterarCep(it) }, label = { Text("CEP") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = CepVisualTransformation(), shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = estado.endereco, onValueChange = { vm.alterarEndereco(it) }, label = { Text("Endereço completo (Opcional)") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())

                        Button(
                            onClick = { vm.salvar() },
                            modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(14.dp)
                        ) { Text("Salvar alterações", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
