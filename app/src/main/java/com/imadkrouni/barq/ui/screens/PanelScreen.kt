package com.imadkrouni.barq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imadkrouni.barq.data.ComponentType
import com.imadkrouni.barq.data.ElectricalComponent
import com.imadkrouni.barq.engine.LoadCalculator
import com.imadkrouni.barq.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelScreen(
    projectId: Long,
    onBack: () -> Unit,
    vm: ProjectViewModel = viewModel()
) {
    LaunchedEffect(projectId) { vm.selectProject(projectId) }
    val components by vm.components.collectAsStateWithLifecycle()

    // اعرض كل القطع القابلة للتحكم
    val controllable = components.filter {
        val t = ComponentType.values().firstOrNull { c -> c.name == it.type }
        t != null && (t.name.contains("LIGHT") || t.name.startsWith("SOCKET") || t.defaultWatts > 0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎛️ لوحة التحكم", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    TextButton(onClick = {
                        controllable.forEach { vm.toggleComponent(it.id, true) }
                    }) { Text("تشغيل الكل", color = Color.White) }
                    TextButton(onClick = {
                        controllable.forEach { vm.toggleComponent(it.id, false) }
                    }) { Text("إطفاء الكل", color = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF212121),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (controllable.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("لا توجد أجهزة قابلة للتحكم\nأضف مصابيح أو أجهزة من المخطط", 
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(controllable, key = { it.id }) { c ->
                    BreakerCard(c) { on -> vm.toggleComponent(c.id, on) }
                }
            }
        }
    }
}

@Composable
fun BreakerCard(c: ElectricalComponent, onToggle: (Boolean) -> Unit) {
    val ct = ComponentType.values().firstOrNull { it.name == c.type } ?: ComponentType.SOCKET_SINGLE
    val current = if (c.voltage > 0) c.watts / c.voltage else 0f
    val breaker = LoadCalculator.pickBreaker(current)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (c.isOn) Color(0xFF1B5E20) else Color(0xFF424242)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(ct.emoji, fontSize = 32.sp)
            Spacer(Modifier.height(4.dp))
            Text(c.label.ifBlank { ct.displayName }, color = Color.White,
                fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("${c.watts.toInt()} W", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            Text("${current.format(1)} A · قاطع $breaker A",
                color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Switch(
                checked = c.isOn,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF66BB6A),
                    uncheckedThumbColor = Color(0xFFEF5350)
                )
            )
        }
    }
}

fun Float.format(d: Int) = "%.${d}f".format(this)
