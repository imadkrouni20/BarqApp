package com.imadkrouni.barq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imadkrouni.barq.data.ComponentType
import com.imadkrouni.barq.data.ElectricalComponent
import com.imadkrouni.barq.data.Wire
import com.imadkrouni.barq.engine.AutoRouter
import com.imadkrouni.barq.ui.components.FloorPlanCanvas
import com.imadkrouni.barq.ui.components.WireRender
import com.imadkrouni.barq.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorPlanScreen(
    projectId: Long,
    onBack: () -> Unit,
    onOpen3D: () -> Unit,
    onOpenPanel: () -> Unit,
    onOpenBOM: () -> Unit,
    vm: ProjectViewModel = viewModel()
) {
    LaunchedEffect(projectId) { vm.selectProject(projectId) }

    val components by vm.components.collectAsState()
    val rooms by vm.rooms.collectAsState()
    val wires by vm.wires.collectAsState()

    var selectedType by remember { mutableStateOf(ComponentType.CEILING_LIGHT) }
    var showWires by remember { mutableStateOf(true) }
    var addMode by remember { mutableStateOf(false) }

    val wireRenders = remember(wires, components) {
        wires.mapNotNull { w ->
            val from = components.firstOrNull { it.id == w.fromComponentId } ?: return@mapNotNull null
            val to = components.firstOrNull { it.id == w.toComponentId } ?: return@mapNotNull null
            WireRender(
                from = Offset(from.x, from.y),
                to = Offset(to.x, to.y),
                color = when (w.color) {
                    "PHASE_BROWN" -> Color(0xFF8D6E63)
                    "NEUTRAL_BLUE" -> Color(0xFF2196F3)
                    "GROUND_GREEN" -> Color(0xFF4CAF50)
                    "SWITCHED_BLACK" -> Color(0xFF212121)
                    "RETURN_RED" -> Color(0xFFF44336)
                    else -> Color.Gray
                },
                length = w.length
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📍 مخطط المبنى", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") }
                },
                actions = {
                    IconButton(onClick = { showWires = !showWires }) {
                        Icon(Icons.Default.FlashOn, "الأسلاك",
                            tint = if (showWires) Color.Yellow else Color.White)
                    }
                    IconButton(onClick = {
                        vm.clearWires()
                        val routes = AutoRouter.computeRoutes(components, 100f, 100f)
                        routes.forEach { r ->
                            vm.saveWire(Wire(
                                projectId = projectId,
                                fromComponentId = r.fromId,
                                toComponentId = r.toId,
                                gauge = r.gauge,
                                color = r.color,
                                length = r.length
                            ))
                        }
                    }) { Icon(Icons.Default.AutoFixHigh, "تركيب تلقائي") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(onClick = onOpen3D, label = { Text("🧊 3D") })
                    AssistChip(onClick = onOpenPanel, label = { Text("🎛️ الطبلون") })
                    AssistChip(onClick = onOpenBOM, label = { Text("📋 الكميات") })
                }

                if (addMode) {
                    LazyRow(
                        Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ComponentType.values().toList()) { t ->
                            FilterChip(
                                selected = selectedType == t,
                                onClick = { selectedType = t },
                                label = { Text("${t.emoji} ${t.displayName}", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { addMode = !addMode }) {
                Icon(if (addMode) Icons.Default.Close else Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            FloorPlanCanvas(
                rooms = rooms,
                components = components,
                wires = wireRenders,
                showWires = showWires,
                modifier = Modifier.fillMaxSize(),
                onCanvasTap = { offset ->
                    if (addMode) {
                        vm.addComponent(ElectricalComponent(
                            projectId = projectId,
                            type = selectedType.name,
                            x = offset.x, y = offset.y,
                            watts = selectedType.defaultWatts,
                            label = selectedType.displayName
                        ))
                    }
                },
                onComponentTap = { comp ->
                    vm.toggleComponent(comp.id, !comp.isOn)
                },
                onComponentDrag = { _, _ -> }
            )
        }
    }
}
