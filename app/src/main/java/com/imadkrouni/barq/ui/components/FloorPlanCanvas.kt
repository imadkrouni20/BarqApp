package com.imadkrouni.barq.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.imadkrouni.barq.data.ComponentType
import com.imadkrouni.barq.data.ElectricalComponent
import com.imadkrouni.barq.data.RoomEntity
import com.imadkrouni.barq.data.Wire

data class WireRender(val from: Offset, val to: Offset, val color: Color, val length: Float)

@Composable
fun FloorPlanCanvas(
    rooms: List<RoomEntity>,
    components: List<ElectricalComponent>,
    wires: List<WireRender>,
    modifier: Modifier = Modifier,
    showWires: Boolean = true,
    showGrid: Boolean = true,
    onComponentTap: ((ElectricalComponent) -> Unit)? = null,
    onCanvasTap: ((Offset) -> Unit)? = null,
    onComponentDrag: ((Long, Offset) -> Unit)? = null
) {
    var animPhase by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16)
            animPhase = (animPhase + 0.02f) % 1f
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(components) {
                detectTapGestures { offset ->
                    val hit = components.minByOrNull { c ->
                        val dx = c.x - offset.x
                        val dy = c.y - offset.y
                        dx * dx + dy * dy
                    }
                    if (hit != null) {
                        val dx = hit.x - offset.x
                        val dy = hit.y - offset.y
                        if (dx * dx + dy * dy < 40 * 40) {
                            onComponentTap?.invoke(hit)
                            return@detectTapGestures
                        }
                    }
                    onCanvasTap?.invoke(offset)
                }
            }
            .pointerInput(components) {
                var draggedId: Long? = null
                detectDragGestures(
                    onDragStart = { offset ->
                        val hit = components.firstOrNull { c ->
                            val dx = c.x - offset.x
                            val dy = c.y - offset.y
                            dx * dx + dy * dy < 40 * 40
                        }
                        draggedId = hit?.id
                    },
                    onDragEnd = { draggedId = null },
                    onDrag = { change, drag ->
                        draggedId?.let { onComponentDrag?.invoke(it, change.position) }
                    }
                )
            }
    ) {
        val gridStep = 50f

        // شبكة
        if (showGrid) {
            var x = 0f
            while (x < size.width) {
                drawLine(Color(0xFFE0E0E0), Offset(x, 0f), Offset(x, size.height), 1f)
                x += gridStep
            }
            var y = 0f
            while (y < size.height) {
                drawLine(Color(0xFFE0E0E0), Offset(0f, y), Offset(size.width, y), 1f)
                y += gridStep
            }
        }

        // الغرف
        rooms.forEach { room ->
            drawRect(
                color = Color(0x1A1565C0),
                topLeft = Offset(room.x, room.y),
                size = Size(room.width, room.height)
            )
            drawRect(
                color = Color(0xFF212121),
                topLeft = Offset(room.x, room.y),
                size = Size(room.width, room.height),
                style = Stroke(width = 6f)
            )
        }

        // الأسلاك مع وميض التيار
        if (showWires) {
            wires.forEach { w ->
                val baseColor = w.color
                drawLine(baseColor, w.from, w.to, strokeWidth = 4f)

                // وميض النبضة
                val segments = 8
                for (i in 0 until segments) {
                    val t = (animPhase + i.toFloat() / segments) % 1f
                    val px = w.from.x + (w.to.x - w.from.x) * t
                    val py = w.from.y + (w.to.y - w.from.y) * t
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = 4f,
                        center = Offset(px, py)
                    )
                }
            }
        }

        // القطع الكهربائية
        components.forEach { c ->
            val ct = ComponentType.values().firstOrNull { it.name == c.type } ?: ComponentType.SOCKET_SINGLE
            val color = when {
                c.isOn && ct.name.contains("LIGHT") -> Color(0xFFFFEB3B)
                c.isOn -> Color(0xFF66BB6A)
                ct.name.contains("LIGHT") -> Color(0xFFFFC107)
                ct.name.startsWith("SWITCH") -> Color(0xFF795548)
                ct.name.startsWith("SOCKET") -> Color(0xFF03A9F4)
                ct.name == "BREAKER" -> Color(0xFFD32F2F)
                ct.name == "FUSE_BOX" -> Color(0xFF212121)
                else -> Color(0xFF9E9E9E)
            }

            // هالة للمصابيح المضاءة
            if (c.isOn && ct.name.contains("LIGHT")) {
                for (r in 1..3) {
                    drawCircle(
                        color = Color(0x33FFEB3B),
                        radius = 20f * r,
                        center = Offset(c.x, c.y)
                    )
                }
            }

            drawCircle(color, radius = 18f, center = Offset(c.x, c.y))
            drawCircle(Color.White, radius = 6f, center = Offset(c.x, c.y))

            // رمز داخل الدائرة
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    textSize = 22f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(ct.emoji, c.x, c.y + 8f, paint)
            }
        }
    }
}
