package com.example.petapp.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.petapp.data.repository.Repositorios
import com.example.petapp.ui.theme.VerdeClaro
import com.example.petapp.ui.theme.VerdePrincipal
import com.example.petapp.viewmodel.PetAppViewModelFactory

private data class Aba(val rotaPadrao: String, val destino: String, val rotulo: String, val icone: ImageVector)

private val abas = listOf(
    Aba(Rotas.INICIO, Rotas.INICIO, "Início", Icons.Default.Home),
    Aba(Rotas.PETS, Rotas.PETS, "Pets", Icons.Default.Pets),
    Aba(Rotas.AGENDA, Rotas.agenda(), "Agenda", Icons.Default.DateRange),
    Aba(Rotas.CONTA, Rotas.CONTA, "Conta", Icons.Default.Person)
)

fun NavController.irParaAba(rota: String) {
    navigate(rota) {
        popUpTo(Rotas.INICIO)
        launchSingleTop = true
    }
}

@Composable
fun PetAppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val fabrica = remember {
        val repositorios = Repositorios.obter(context)
        PetAppViewModelFactory(
            usuarioRepository = repositorios.usuarioRepository,
            petRepository = repositorios.petRepository,
            petshopRepository = repositorios.petshopRepository,
            agendamentoRepository = repositorios.agendamentoRepository
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination?.route
    val mostrarBarra = rotaAtual != null && rotaAtual != Rotas.LOGIN && rotaAtual != Rotas.CADASTRO

    val abaSelecionada = when (rotaAtual) {
        Rotas.DETALHE_PET -> Rotas.PETS
        Rotas.DETALHE_AGENDAMENTO -> Rotas.AGENDA
        Rotas.EDITAR_PERFIL -> Rotas.CONTA
        else -> rotaAtual
    }

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar(containerColor = Color.White) {
                    abas.forEach { aba ->
                        NavigationBarItem(
                            selected = abaSelecionada == aba.rotaPadrao,
                            onClick = { navController.irParaAba(aba.destino) },
                            icon = { Icon(aba.icone, contentDescription = aba.rotulo) },
                            label = { Text(aba.rotulo, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = VerdePrincipal, selectedTextColor = VerdePrincipal, indicatorColor = VerdeClaro, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Rotas.LOGIN, modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {

            composable(Rotas.LOGIN) {
                LoginScreen(
                    fabrica = fabrica,
                    onNavigateToCadastro = { navController.navigate(Rotas.CADASTRO) },
                    onNavigateToHome = { navController.navigate(Rotas.INICIO) { popUpTo(Rotas.LOGIN) { inclusive = true } } }
                )
            }

            composable(Rotas.CADASTRO) {
                CadastroScreen(fabrica = fabrica, onNavigateBackToLogin = { navController.popBackStack() })
            }

            composable(Rotas.INICIO) {
                InicioScreen(
                    fabrica = fabrica,
                    onServicoClick = { servico -> navController.irParaAba(Rotas.agenda(servico = servico)) },
                    onPetClick = { id -> navController.navigate(Rotas.detalhePet(id)) },
                    onPetsClick = { navController.irParaAba(Rotas.PETS) },
                    onAgendaClick = { navController.irParaAba(Rotas.agenda()) },
                    onContaClick = { navController.irParaAba(Rotas.CONTA) }
                )
            }

            composable(Rotas.PETS) {
                PetsScreen(fabrica = fabrica, onPetClick = { id -> navController.navigate(Rotas.detalhePet(id)) })
            }

            composable(
                route = Rotas.AGENDA,
                arguments = listOf(
                    navArgument("servico") { type = NavType.StringType; defaultValue = "" },
                    navArgument("petId") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { entry ->
                AgendaScreen(
                    fabrica = fabrica,
                    servicoInicial = entry.arguments?.getString("servico") ?: "",
                    petInicial = entry.arguments?.getInt("petId") ?: -1,
                    onAgendamentoClick = { id -> navController.navigate(Rotas.detalheAgendamento(id)) }
                )
            }

            composable(Rotas.CONTA) {
                ContaScreen(
                    fabrica = fabrica,
                    onMeusPets = { navController.irParaAba(Rotas.PETS) },
                    onMinhaAgenda = { navController.irParaAba(Rotas.agenda()) },
                    onEditarPerfil = { navController.navigate(Rotas.EDITAR_PERFIL) },
                    onSair = { navController.navigate(Rotas.LOGIN) { popUpTo(Rotas.INICIO) { inclusive = true } } }
                )
            }

            composable(Rotas.EDITAR_PERFIL) {
                EditarPerfilScreen(fabrica = fabrica, onVoltar = { navController.popBackStack() })
            }

            composable(route = Rotas.DETALHE_PET, arguments = listOf(navArgument("petId") { type = NavType.IntType })) { entry ->
                DetalhePetScreen(
                    fabrica = fabrica,
                    petId = entry.arguments?.getInt("petId") ?: -1,
                    onVoltar = { navController.popBackStack() },
                    onAgendamentoClick = { id -> navController.navigate(Rotas.detalheAgendamento(id)) },
                    onAgendarClick = { petId -> navController.irParaAba(Rotas.agenda(petId = petId)) }
                )
            }

            composable(route = Rotas.DETALHE_AGENDAMENTO, arguments = listOf(navArgument("agendamentoId") { type = NavType.IntType })) { entry ->
                DetalheAgendamentoScreen(
                    fabrica = fabrica,
                    agendamentoId = entry.arguments?.getInt("agendamentoId") ?: -1,
                    onVoltar = { navController.popBackStack() },
                    onPetClick = { id -> navController.navigate(Rotas.detalhePet(id)) }
                )
            }
        }
    }
}
