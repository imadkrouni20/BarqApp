package com.imadkrouni.barq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imadkrouni.barq.data.*
import com.imadkrouni.barq.engine.AutoRouter
import com.imadkrouni.barq.ui.components.FloorPlanCanvas
import com.imadkrouni.barq.ui.components.WireRender
import com.imadkrouni.barq.viewmodel.ProjectViewModel

enum class ToolMode { SELECT, ADD_COMPONENT, ADD_ROOM, DELETE }

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

    var mode by remember { mutableStateOf(ToolMode.SELECT) }
    var selectedType by remember { mutableStateOf(ComponentType.CEILING_LIGHT) }
    var selectedCategory by remember { mutableStateOf("LIGHT") }
    var showWires by remember { mutableStateOf(true) }
    var showRoomDialog by remember { mutableStateOf(false) }
    var selectedComponent by remember { mutableStateOf<ElectricalComponent?>(null) }
    var showInfoPanel by remember { mutableStateOf(false) }

    // Drag state for room drawing
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }

    // Scale: 50 px = 1 meter
    val pxPerMeter = 50f

    // حاسبة التكلفة التقريبية
    val estimatedCost = remember(components) {
        components.sumOf { c ->
            when {
                c.type.contains("LIGHT") || c.type == "CHANDELIER" -> 80.0
                c.type.startsWith("SOCKET") -> 45.0
                c.type.startsWith("SWITCH") -> 35.0
                c.type == "WATER_HEATER" -> 1500.0
                c.type == "AC_UNIT" -> 3500.0
                c.type == "BREAKER" || c.type == "RCD" -> 120.0
                else -> 100.0
            }
        }
    }

    // فئات القطع
    val categories = listOf(
        "LIGHT" to "💡 إضاءة",
        "SOCKET" to "🔌 مقابس",
        "SWITCH" to "🎚️ مفاتيح",
        "HEAVY" to "⚡ أجهزة",
        "PROTECTION" to "🛡️ حماية"
    )

    val filteredTypes = remember(selectedCategory) {
        when (selectedCategory) {
            "LIGHT" -> listOf(ComponentType.CEILING_LIGHT, ComponentType.WALL_LIGHT,
                              ComponentType.CHANDELIER, ComponentType.SPOTLIGHT)
            "SOCKET" -> listOf(ComponentType.SOCKET_SINGLE, ComponentType.SOCKET_DOUBLE,
                               ComponentType.SOCKET_GROUNDED, ComponentType.TV_POINT,
                               ComponentType.INTERNET_POINT)
            "SWITCH" -> listOf(ComponentType.SWITCH_1WAY, ComponentType.SWITCH_2WAY)
            "HEAVY" -> listOf(ComponentType.WATER_HEATER, ComponentType.AC_UNIT,
                              ComponentType.OVEN, ComponentType.WASHING_MACHINE,
                              ComponentType.FRIDGE, ComponentType.DISHWASHER,
                              ComponentType.MICROWAVE)
            "PROTECTION" -> listOf(ComponentType.BREAKER, ComponentType.RCD,
                                   ComponentType.FUSE_BOX)
            else -> ComponentType.values().toList()
        }
    }

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
                title = {
                    Column {
                        Text("📍 مخطط المبنى", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${rooms.size} غرفة · ${components.size} قطعة · ~${estimatedCost.toInt()} DH",
                            fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") }
                },
                actions = {
                    // Undo
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Undo, "تراجع")
                    }
                    // Toggle wires
                    IconButton(onClick = { showWires = !showWires }) {
                        Icon(Icons.Default.FlashOn, "الأسلاك",
                            tint = if (showWires) Color.Yellow else Color.White)
                    }
                    // Auto route
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
                    }) {
                        Icon(Icons.Default.AutoFixHigh, "تركيب تلقائي")
                    }
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

                // شريط الأدوات
                Row(
                    Modifier.fillMaxWidth().padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ToolChip("👆 تحديد", mode == ToolMode.SELECT) { mode = ToolMode.SELECT }
                    ToolChip("➕ إضافة", mode == ToolMode.ADD_COMPONENT) { mode = ToolMode.ADD_COMPONENT }
                    ToolChip("🏗️ غرفة", mode == ToolMode.ADD_ROOM) { mode = ToolMode.ADD_ROOM }
                    ToolChip("🗑️ حذف", mode == ToolMode.DELETE) { mode = ToolMode.DELETE }
                }

                // فئات القطع
                if (mode == ToolMode.ADD_COMPONENT) {
                    LazyRow(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { (key, label) ->
                            FilterChip(
                                selected = selectedCategory == key,
                                onClick = { selectedCategory = key },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    LazyRow(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredTypes) { t ->
                            FilterChip(
                                selected = selectedType == t,
                                onClick = { selectedType = t },
                                label = { Text("${t.emoji} ${t.displayName}", fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // معلومات وضع الرسم
                if (mode == ToolMode.ADD_ROOM) {
                    Surface(
                        Modifier.fillMaxWidth().padding(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "🏗️ اسحب على الشاشة لرسم غرفة بحجم حقيقي (50 بكسل = 1 متر)",
                            Modifier.padding(8.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                // أزرار التنقل السريع + السيناريوهات
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(onClick = onOpen3D, label = { Text("🧊 3D") })
                    AssistChip(onClick = onOpenPanel, label = { Text("🎛️ الطبلون") })
                    AssistChip(onClick = onOpenBOM, label = { Text("📋 الكميات") })
                    AssistChip(onClick = { showRoomDialog = true }, label = { Text("🏗️+") })
                }

                // سيناريوهات
                LazyRow(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item { ScenarioChip("🌅 استقبال", Color(0xFFFFA726)) { vm.applyScenario("ENTRY") } }
                    item { ScenarioChip("💤 نوم", Color(0xFF5C6BC0)) { vm.applyScenario("SLEEP") } }
                    item { ScenarioChip("🎬 سينما", Color(0xFF8E24AA)) { vm.applyScenario("CINEMA") } }
                    item { ScenarioChip("🎉 حفلة", Color(0xFFE91E63)) { vm.applyScenario("PARTY") } }
                    item { ScenarioChip("🔴 كل شيء", Color(0xFFD32F2F)) { vm.applyScenario("ALL_ON") } }
                    item { ScenarioChip("⚫ إطفاء", Color(0xFF424242)) { vm.applyScenario("ALL_OFF") } }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Color.White)) {

            FloorPlanCanvas(
                rooms = rooms,
                components = components,
                wires = wireRenders,
                showWires = showWires,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(mode) {
                        if (mode == ToolMode.ADD_ROOM) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    dragStart = offset
                                    dragCurrent = offset
                                },
                                onDrag = { change, _ ->
                                    dragCurrent = change.position
                                },
                                onDragEnd = {
                                    val s = dragStart
                                    val e = dragCurrent
                                    if (s != null && e != null) {
                                        val w = kotlin.math.abs(e.x - s.x)
                                        val h = kotlin.math.abs(e.y - s.y)
                                        if (w > 30 && h > 30) {
                                            vm.addRoom(RoomEntity(
                                                projectId = projectId,
                                                name = "غرفة ${rooms.size + 1}",
                                                type = "BEDROOM",
                                                x = minOf(s.x, e.x),
                                                y = minOf(s.y, e.y),
                                                width = w, height = h
                                            ))
                                        }
                                    }
                                    dragStart = null
                                    dragCurrent = null
                                }
                            )
                        }
                    },
                onCanvasTap = { offset ->
                    when (mode) {
                        ToolMode.ADD_COMPONENT -> {
                            vm.addComponent(ElectricalComponent(
                                projectId = projectId,
                                type = selectedType.name,
                                x = offset.x, y = offset.y,
                                watts = selectedType.defaultWatts,
                                label = selectedType.displayName
                            ))
                        }
                        ToolMode.DELETE -> { /* handled by onComponentTap */ }
                        else -> {}
                    }
                },
                onComponentTap = { comp ->
                    when (mode) {
                        ToolMode.DELETE -> vm.deleteComponent(comp)
                        ToolMode.SELECT -> {
                            selectedComponent = comp
                            showInfoPanel = true
                        }
                        else -> vm.toggleComponent(comp.id, !comp.isOn)
                    }
                },
                onComponentDrag = { _, _ -> }
            )

            // نافذة معلومات القطعة
            selectedComponent?.let { comp ->
                if (showInfoPanel) {
                    ComponentInfoSheet(
                        component = comp,
                        onDismiss = { showInfoPanel = false; selectedComponent = null },
                        onToggle = { vm.toggleComponent(comp.id, !comp.isOn) },
                        onDuplicate = { vm.duplicateComponent(comp); showInfoPanel = false },
                        onDelete = { vm.deleteComponent(comp); showInfoPanel = false }
                    )
                }
            }
        }
    }

    if (showRoomDialog) {
        RoomEditorDialog(
            onDismiss = { showRoomDialog = false },
            onConfirm = { name, type, wM, hM ->
                vm.addRoom(RoomEntity(
                    projectId = projectId,
                    name = name,
                    type = type,
                    x = 100f, y = 100f,
                    width = wM * pxPerMeter,
                    height = hM * pxPerMeter
                ))
                showRoomDialog = false
            }
        )
    }
}

@Composable
fun ToolChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) }
    )
}

@Composable
fun ScenarioChip(label: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = color
    ) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ComponentInfoSheet(
    component: ElectricalComponent,
    onDismiss: () -> Unit,
    onToggle: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val ct = ComponentType.values().firstOrNull { it.name == component.type } ?: ComponentType.SOCKET_SINGLE
    val current = if (component.voltage > 0) component.watts / component.voltage else 0f
    val breaker = com.imadkrouni.barq.engine.LoadCalculator.pickBreaker(current)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${ct.emoji} ${component.label.ifBlank { ct.displayName }}") },
        text = {
            Column {
                InfoRow("النوع", ct.displayName)
                InfoRow("القدرة", "${component.watts.toInt()} W")
                InfoRow("الجهد", "${component.voltage.toInt()} V")
                InfoRow("التيار", "%.1f A".format(current))
                InfoRow("القاطع المقترح", "$breaker A")
                InfoRow("الحالة", if (component.isOn) "✅ مشتغل" else "⚫ مطفأ")
                InfoRow("الموقع", "X:${component.x.toInt()} Y:${component.y.toInt()}")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDuplicate) { Text("📋 نسخ") }
                TextButton(onClick = onDelete) { Text("🗑️ حذف", color = Color.Red) }
                Button(onClick = onToggle) {
                    Text(if (component.isOn) "إطفاء" else "تشغيل")
                }
            }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
