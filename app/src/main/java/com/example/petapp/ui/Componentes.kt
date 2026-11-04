package com.example.petapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bed
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petapp.ui.theme.VerdePrincipal


@Composable
fun FotoPerfil(uri: String, modifier: Modifier = Modifier, fallbackIcon: ImageVector = Icons.Rounded.Pets) {
    if (uri.isNotBlank()) {
        AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier.clip(CircleShape).background(Color(0xFFE8E8E8)))
    } else {
        Box(modifier = modifier.clip(CircleShape).background(Color(0xFF7CB39E)), contentAlignment = Alignment.Center) {
            Icon(fallbackIcon, contentDescription = null, tint = Color.White)
        }
    }
}

fun iconeServico(servico: String): ImageVector = when (servico) {
    "Banho" -> Icons.Rounded.WaterDrop
    "Tosa" -> Icons.Rounded.ContentCut
    "Vacinas" -> Icons.Rounded.MedicalServices
    "Consulta" -> Icons.Rounded.MonitorHeart
    "Hotel" -> Icons.Rounded.Bed
    "Táxi pet" -> Icons.Rounded.LocalTaxi
    else -> Icons.Rounded.Pets
}

@Composable
fun coresCampo() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = VerdePrincipal,
    unfocusedBorderColor = Color.LightGray
)

@Composable
fun CartaoSecao(titulo: String, conteudo: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            conteudo()
        }
    }
}

@Composable
fun LinhaInfo(rotulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(rotulo, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(valor, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
