@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp.ui

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petapp.ui.theme.VerdePrincipal
import com.example.petapp.ui.theme.VermelhoAlerta
import com.example.petapp.viewmodel.CadastroViewModel

@Composable
fun CadastroScreen(
    fabrica: ViewModelProvider.Factory,
    onNavigateBackToLogin: () -> Unit,
    vm: CadastroViewModel = viewModel(factory = fabrica)
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(estado.mensagem) {
        estado.mensagem?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.mensagemExibida()
        }
    }
    LaunchedEffect(estado.cadastroConcluido) {
        if (estado.cadastroConcluido) {
            vm.navegacaoConcluida()
            onNavigateBackToLogin()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFFFFF)).padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.Start) {
        Spacer(modifier = Modifier.height(24.dp))
        IconButton(onClick = onNavigateBackToLogin, modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Criar conta", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = "Leva menos de um minuto.", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Nome completo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = estado.nome, onValueChange = { vm.alterarNome(it) }, placeholder = { Text("Ex: João Silva") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("CPF", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = estado.cpf, onValueChange = { vm.alterarCpf(it) }, placeholder = { Text("000.000.000-00") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = CpfVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("E-mail", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = estado.email, onValueChange = { vm.alterarEmail(it) }, placeholder = { Text("usuario@email.com") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Celular", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = estado.celular, onValueChange = { vm.alterarCelular(it) }, placeholder = { Text("(00) 00000-0000") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = TelefoneVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Senha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = estado.senha, onValueChange = { vm.alterarSenha(it) }, placeholder = { Text("Mínimo 6 caracteres") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Confirmar Senha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = estado.confirmarSenha, onValueChange = { vm.alterarConfirmarSenha(it) }, placeholder = { Text("Repita sua senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = estado.aceiteTermos, onCheckedChange = { vm.alterarAceiteTermos(it) }, colors = CheckboxDefaults.colors(checkedColor = VerdePrincipal))
            Text("Aceito os Termos de uso e a Política de privacidade.", fontSize = 12.sp)
        }

        if (estado.erro.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(estado.erro, color = VermelhoAlerta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { vm.cadastrar() },
            enabled = estado.aceiteTermos,
            modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(16.dp)
        ) { Text("Cadastrar", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Já tenho conta · ", color = Color.Gray)
            Text("Entrar", color = VerdePrincipal, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateBackToLogin() })
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
