package com.imadkrouni.barq.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imadkrouni.barq.data.ComponentType
import com.imadkrouni.barq.viewmodel.ProjectViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreeDViewScreen(
    projectId: Long,
    onBack: () -> Unit,
    vm: ProjectViewModel = viewModel()
) {
    LaunchedEffect(projectId) { vm.selectProject(projectId) }
    val components by vm.components.collectAsStateWithLifecycle()
    val rooms by vm.rooms.collectAsStateWithLifecycle()

    var angleX by remember { mutableStateOf(0.6f) }
    var angleY by remember { mutableStateOf(0.4f) }
    var scale by remember { mutableStateOf(1f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧊 عرض ثلاثي الأبعاد", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Canvas(
                Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, drag ->
                            angleY += drag.x * 0.01f
                            angleX = (angleX + drag.y * 0.01f).coerceIn(-1.4f, 1.4f)
                        }
                    }
            ) {
                val cx = size.width / 2
                val cy = size.height / 2
                val s = 0.6f * scale

                // ارسم الغرف كصناديق معزولة
                rooms.forEach { r ->
                    val corners = listOf(
                        r.x to r.y, r.x + r.width to r.y,
                        r.x + r.width to r.y + r.height, r.x to r.y + r.height
                    )
                    val projected = corners.map { (x, y) -> project(x, y, 0f, cx, cy, angleX, angleY, s) }
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(projected[0].x, projected[0].y)
                        projected.drop(1).forEach { lineTo(it.x, it.y) }
                        close()
                    }
                    drawPath(path, Color(0x331565C0))
                    drawPath(path, Color(0xFF1565C0), style = Stroke(3f))

                    // ارفع للسقف
                    val topCorners = corners.map { (x, y) -> project(x, y, 300f, cx, cy, angleX, angleY, s) }
                    topCorners.forEachIndexed { i, tc ->
                        drawLine(Color(0x551565C0), projected[i], tc, 1f)
                    }
                    val topPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(topCorners[0].x, topCorners[0].y)
                        topCorners.drop(1).forEach { lineTo(it.x, it.y) }
                        close()
                    }
                    drawPath(topPath, Color(0x221565C0), style = Stroke(2f))
                }

                // القطع
                components.forEach { c ->
                    val p = project(c.x, c.y, c.z + 150f, cx, cy, angleX, angleY, s)
                    val ct = ComponentType.values().firstOrNull { it.name == c.type } ?: ComponentType.SOCKET_SINGLE
                    val color = when {
                        c.isOn && ct.name.contains("LIGHT") -> Color(0xFFFFEB3B)
                        c.isOn -> Color(0xFF66BB6A)
                        ct.name.contains("LIGHT") -> Color(0xFFFFC107)
                        else -> Color(0xFF03A9F4)
                    }

                    if (c.isOn && ct.name.contains("LIGHT")) {
                        for (r in 1..4) {
                            drawCircle(Color(0x22FFEB3B), radius = 30f * r, center = p)
                        }
                    }
                    drawCircle(color, 14f, p)
                    drawCircle(Color.White, 4f, p)
                }
            }

            // شريط التحكم
            Surface(
                Modifier.align(androidx.compose.ui.Alignment.BottomCenter).fillMaxWidth(),
                color = Color(0xCC000000)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("🖱️ اسحب للدوران", color = Color.White, fontSize = 12.sp)
                    Row {
                        Text("تكبير: ", color = Color.White)
                        Slider(value = scale, onValueChange = { scale = it }, valueRange = 0.3f..2f,
                            modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// إسقاط 3D → 2D
private fun project(
    x: Float, y: Float, z: Float,
    cx: Float, cy: Float,
    angleX: Float, angleY: Float, scale: Float
): Offset {
    // rotate Y
    val x1 = x * cos(angleY) - z * sin(angleY)
    val z1 = x * sin(angleY) + z * cos(angleY)
    // rotate X
    val y2 = y * cos(angleX) - z1 * sin(angleX)
    return Offset(cx + x1 * scale, cy + y2 * scale)
}
