package com.imadkrouni.barq.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// ============ المشروع ============
@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val clientName: String = "",
    val clientPhone: String = "",
    val address: String = "",
    val area: Float = 0f,
    val floors: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ============ الغرف ============
@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val type: String = "BEDROOM",
    val x: Float, val y: Float,
    val width: Float, val height: Float
)

// ============ القطع الكهربائية ============
@Entity(tableName = "components")
data class ElectricalComponent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long = 0,
    val type: String,
    val x: Float, val y: Float, val z: Float = 0f,
    val watts: Float = 0f,
    val voltage: Float = 220f,
    val isOn: Boolean = false,
    val label: String = "",
    val linkedSwitchId: Long = -1
)

// ============ الأسلاك ============
@Entity(tableName = "wires")
data class Wire(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val fromComponentId: Long,
    val toComponentId: Long,
    val gauge: Float = 2.5f,
    val color: String = "PHASE_BROWN",
    val length: Float = 0f,
    val pathJson: String = ""
)

// ============ الأنواع ============
enum class RoomType { BEDROOM, LIVING_ROOM, KITCHEN, BATHROOM, HALLWAY, GARAGE, OFFICE, STORAGE, BALCONY }

enum class ComponentType(val displayName: String, val defaultWatts: Float, val emoji: String) {
    CEILING_LIGHT("مصباح سقف", 20f, "💡"),
    WALL_LIGHT("مصباح جدار", 40f, "🔆"),
    CHANDELIER("ثريا", 200f, "✨"),
    SPOTLIGHT("سبوت", 10f, "🔦"),
    SOCKET_SINGLE("مقبس", 0f, "🔌"),
    SOCKET_DOUBLE("مقبس مزدوج", 0f, "🔌"),
    SOCKET_GROUNDED("مقبس أرضي", 0f, "🔌"),
    SWITCH_1WAY("مفتاح", 0f, "🎚️"),
    SWITCH_2WAY("مفتاح مزدوج", 0f, "🎚️"),
    WATER_HEATER("سخان", 2000f, "🚿"),
    AC_UNIT("مكيف", 1800f, "❄️"),
    OVEN("فرن", 2500f, "🔥"),
    WASHING_MACHINE("غسالة", 2200f, "🌀"),
    FRIDGE("ثلاجة", 200f, "🧊"),
    DISHWASHER("غسالة أطباق", 1500f, "🍽️"),
    MICROWAVE("مايكروويف", 1200f, "📻"),
    BREAKER("قاطع", 0f, "🔴"),
    RCD("قاطع تفاضلي", 0f, "🛡️"),
    FUSE_BOX("لوحة توزيع", 0f, "⚡"),
    TV_POINT("نقطة تلفاز", 0f, "📺"),
    INTERNET_POINT("نقطة إنترنت", 0f, "🌐")
}

enum class WireColor(val hex: Long) {
    PHASE_BROWN(0xFF8D6E63),
    NEUTRAL_BLUE(0xFF2196F3),
    GROUND_GREEN(0xFF4CAF50),
    SWITCHED_BLACK(0xFF212121),
    RETURN_RED(0xFFF44336)
}
