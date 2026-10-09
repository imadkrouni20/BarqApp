package com.imadkrouni.barq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RoomTemplate(val type: String, val label: String, val emoji: String, val area: Float)

val ROOM_TEMPLATES = listOf(
    RoomTemplate("BEDROOM",     "غرفة نوم",   "🛏️", 16f),
    RoomTemplate("LIVING_ROOM", "صالة",       "🛋️", 24f),
    RoomTemplate("KITCHEN",     "مطبخ",       "🍳", 12f),
    RoomTemplate("BATHROOM",    "حمام",       "🚿", 6f),
    RoomTemplate("HALLWAY",     "ممر",        "🚪", 8f),
    RoomTemplate("GARAGE",      "مرآب",       "🚗", 20f),
    RoomTemplate("OFFICE",      "مكتب",       "💼", 15f),
    RoomTemplate("STORAGE",     "مخزن",       "📦", 8f),
    RoomTemplate("BALCONY",     "شرفة",       "🌿", 6f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomEditorDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, widthM: Float, heightM: Float) -> Unit
) {
    var selected by remember { mutableStateOf(ROOM_TEMPLATES[0]) }
    var customName by remember { mutableStateOf("") }
    var widthM by remember { mutableStateOf("4") }
    var heightM by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🏗️ إضافة غرفة جديدة") },
        text = {
            Column {
                Text("اختر نوع الغرفة:", fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ROOM_TEMPLATES) { t ->
                        FilterChip(
                            selected = selected == t,
                            onClick = { selected = t; customName = "" },
                            label = { Text("${t.emoji} ${t.label}", fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("اسم مخصص (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = widthM,
                        onValueChange = { widthM = it },
                        label = { Text("العرض (م)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = heightM,
                        onValueChange = { heightM = it },
                        label = { Text("الطول (م)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("📐 المساحة: ${(widthM.toFloatOrNull() ?: 0f) * (heightM.toFloatOrNull() ?: 0f)} م²",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(onClick = {
                val name = customName.ifBlank { selected.label }
                onConfirm(name, selected.type,
                    widthM.toFloatOrNull() ?: 4f,
                    heightM.toFloatOrNull() ?: 4f)
            }) { Text("إضافة") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
