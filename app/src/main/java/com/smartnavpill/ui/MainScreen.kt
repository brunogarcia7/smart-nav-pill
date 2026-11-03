package com.smartnavpill.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartnavpill.data.NavPillSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    serviceEnabled: Boolean,
    onOpenAccessibility: () -> Unit,
    onPickApp: (GestureSlot) -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val active = serviceEnabled && settings.enabled

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Smart Nav Pill", fontWeight = FontWeight.SemiBold)
            })
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Elemento assinatura: representa a barrinha de navegacao e o estado
            // atual do recurso, comunicando de imediato o que o app faz.
            PillHero(active = active)

            if (!serviceEnabled) {
                ServiceWarningCard(onOpenAccessibility)
            }

            MasterSwitchCard(
                enabled = settings.enabled,
                onToggle = viewModel::setEnabled,
                serviceEnabled = serviceEnabled
            )

            SectionHeader("Gestos")

            GestureCard(
                icon = Icons.Rounded.TouchApp,
                title = "Toque duplo",
                description = "Tocar duas vezes rápido na barrinha.",
                appLabel = viewModel.labelFor(settings.doubleTapPackage),
                onClick = { onPickApp(GestureSlot.DOUBLE_TAP) }
            )

            GestureCard(
                icon = Icons.Rounded.Gesture,
                title = "Toque longo",
                description = "Pressionar e segurar na barrinha.",
                appLabel = viewModel.labelFor(settings.longPressPackage),
                onClick = { onPickApp(GestureSlot.LONG_PRESS) }
            )

            SectionHeader("Área de toque")

            AreaCard(settings = settings, viewModel = viewModel)

            VibrateCard(enabled = settings.vibrate, onToggle = viewModel::setVibrate)

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Hero com a barrinha estilizada. A "pill" acende na cor de destaque quando o
 * recurso esta ativo e fica apagada quando nao esta, dando um retorno visual
 * imediato do estado — o elemento que torna a tela reconhecivel.
 */
@Composable
private fun PillHero(active: Boolean) {
    val pillColor by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        label = "pillColor"
    )
    val pillWidth by animateDpAsState(
        targetValue = if (active) 132.dp else 108.dp,
        label = "pillWidth"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Representacao de um "telefone" com a barrinha na base.
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(96.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .width(pillWidth.coerceAtMost(96.dp))
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(pillColor)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (active) Icons.Rounded.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (active) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (active) "Ativo — toque na barrinha para abrir seus apps"
                    else "Inativo — configure e ative abaixo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun ServiceWarningCard(onOpenAccessibility: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Serviço de acessibilidade desativado",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Text(
                "Para o Smart Nav Pill funcionar, ative o serviço em " +
                    "Configurações > Acessibilidade > Smart Nav Pill.",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onOpenAccessibility) {
                Text("Abrir Acessibilidade")
            }
        }
    }
}

@Composable
private fun MasterSwitchCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    serviceEnabled: Boolean,
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Ativar recurso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (serviceEnabled) "Toques na barrinha abrem os apps configurados."
                    else "Ative primeiro o serviço de acessibilidade.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked = enabled, onCheckedChange = onToggle, enabled = serviceEnabled)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GestureCard(
    icon: ImageVector,
    title: String,
    description: String,
    appLabel: String?,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val hasApp = appLabel != null
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icone em um recipiente arredondado, com rotulo para leitor de tela.
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (hasApp) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (hasApp) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!hasApp) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        appLabel ?: "Escolher app",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasApp) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AreaCard(settings: NavPillSettings, viewModel: MainViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Modo recomendado: faixa estreita centralizada so sobre a barrinha.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Centralizar na barrinha",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "A faixa cobre apenas a área da barrinha de início, no centro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.centerOnPill,
                    onCheckedChange = viewModel::setCenterOnPill
                )
            }

            SliderRow(
                label = "Largura",
                valueText = "${settings.widthPercent}% da tela",
                value = settings.widthPercent.toFloat(),
                range = 10f..100f,
                enabled = !settings.centerOnPill,
                onChange = { viewModel.setWidthPercent(it.toInt()) }
            )

            SliderRow(
                label = "Altura",
                valueText = "${settings.heightDp} dp",
                value = settings.heightDp.toFloat(),
                range = 12f..64f,
                onChange = { viewModel.setHeightDp(it.toInt()) }
            )

            SliderRow(
                label = "Tempo do toque longo",
                valueText = "${settings.longPressMs} ms",
                value = settings.longPressMs.toFloat(),
                range = 200f..800f,
                onChange = { viewModel.setLongPressMs(it.toInt()) }
            )

            Text(
                "A faixa fica na borda inferior, sobre a barra de navegação. " +
                    "Centralizar reduz toques acidentais; um tempo menor deixa o " +
                    "toque longo mais rápido.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean = true,
    onChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                valueText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            enabled = enabled
        )
    }
}

@Composable
private fun VibrateCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Vibration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Vibrar ao acionar",
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}
