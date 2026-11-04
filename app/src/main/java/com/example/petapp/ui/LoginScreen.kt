@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petapp.R
import com.example.petapp.ui.theme.FundoApp
import com.example.petapp.ui.theme.VerdePrincipal
import com.example.petapp.ui.theme.VermelhoAlerta
import com.example.petapp.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    fabrica: ViewModelProvider.Factory,
    onNavigateToCadastro: () -> Unit,
    onNavigateToHome: () -> Unit,
    vm: LoginViewModel = viewModel(factory = fabrica)
) {

    val estado by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(estado.mensagem) {
        estado.mensagem?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.mensagemExibida()
        }
    }

    LaunchedEffect(estado.loginRealizado) {
        if (estado.loginRealizado) {
            vm.navegacaoConcluida()
            onNavigateToHome()
        }
    }

    if (estado.mostrarEsqueciSenha) {
        AlertDialog(
            onDismissRequest = { vm.fecharEsqueciSenha() },
            title = { Text("Redefinir senha") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = estado.emailRecuperacao, onValueChange = { vm.alterarEmailRecuperacao(it) }, label = { Text("Seu e-mail cadastrado") }, singleLine = true)
                    OutlinedTextField(value = estado.novaSenhaRecuperacao, onValueChange = { vm.alterarNovaSenha(it) }, label = { Text("Nova senha (mín. 6 caracteres)") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                }
            },
            confirmButton = { TextButton(onClick = { vm.salvarNovaSenha() }) { Text("Salvar nova senha", color = VerdePrincipal) } },
            dismissButton = { TextButton(onClick = { vm.fecharEsqueciSenha() }) { Text("Cancelar") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoApp).padding(24.dp), horizontalAlignment = Alignment.Start) {
        Spacer(modifier = Modifier.height(40.dp))
        Image(painter = painterResource(id = R.drawable.ic_logo), contentDescription = "Logo", modifier = Modifier.size(60.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Bem-vindo de\nvolta", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black, lineHeight = 36.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Entre para cuidar do seu melhor amigo.", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        Text("E-mail ou CPF", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        // Só decide a máscara visual (é desenho, não regra).
        val isCpf = estado.emailOuCpf.isNotEmpty() && estado.emailOuCpf.all { it.isDigit() }
        OutlinedTextField(
            value = estado.emailOuCpf,
            onValueChange = { vm.alterarEmailOuCpf(it) }, // ...e só AVISA o ViewModel quando o usuário digita.
            placeholder = { Text("Digite um dos dois") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            visualTransformation = if (isCpf) CpfVisualTransformation() else VisualTransformation.None,
            keyboardOptions = if (isCpf) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal, unfocusedBorderColor = Color.LightGray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Senha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = estado.senha, onValueChange = { vm.alterarSenha(it) }, placeholder = { Text("••••••") }, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal, unfocusedBorderColor = VerdePrincipal))

        if (estado.erro.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(estado.erro, color = VermelhoAlerta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = estado.lembrarMim, onCheckedChange = { vm.alterarLembrarMim(it) }, colors = CheckboxDefaults.colors(checkedColor = VerdePrincipal))
                Text("Lembrar de mim", fontSize = 14.sp)
            }
            Text("Esqueci a senha", color = VerdePrincipal, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { vm.abrirEsqueciSenha() })
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { vm.entrar() },
            modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(16.dp)
        ) { Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().clickable { onNavigateToCadastro() }.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Não tem conta? ", color = Color.Gray)
            Text("Cadastre-se", color = VerdePrincipal, fontWeight = FontWeight.Bold)
        }
    }
}
