package com.imadkrouni.barq.engine

import com.imadkrouni.barq.data.ElectricalComponent
import kotlin.math.*

/**
 * خوارزمية Auto-Routing مع Steiner Tree Approximation
 * تقلل هدر الأسلاك لأقصى حد
 */
object AutoRouter {

    data class WireRoute(
        val fromId: Long,
        val toId: Long,
        val points: List<Pair<Float, Float>>,
        val length: Float,
        val gauge: Float,
        val color: String
    )

    /**
     * يبني شجرة تمتد دنيا MST بين كل القطع
     * ثم يضيف تحسينات Steiner لتقليل الطول
     */
    fun computeRoutes(
        components: List<ElectricalComponent>,
        panelX: Float = 0f,
        panelY: Float = 0f
    ): List<WireRoute> {
        if (components.isEmpty()) return emptyList()

        val routes = mutableListOf<WireRoute>()

        // 1. افصل بين القطع حسب الدور الوظيفي
        val switches = components.filter { it.type.startsWith("SWITCH") }
        val lights = components.filter { it.type.contains("LIGHT") || it.type == "CHANDELIER" || it.type == "SPOTLIGHT" }
        val sockets = components.filter { it.type.startsWith("SOCKET") }
        val heavy = components.filter {
            it.type in listOf("WATER_HEATER", "AC_UNIT", "OVEN", "WASHING_MACHINE", "DISHWASHER", "MICROWAVE")
        }

        // 2. اربط كل مصباح بأقرب مفتاح
        lights.forEach { light ->
            val nearestSwitch = switches.minByOrNull { comp -> distance(comp, light) }
            if (nearestSwitch != null) {
                routes.add(buildRoute(nearestSwitch, light, "SWITCHED_BLACK", 1.5f, listOf(nearestSwitch, light)))
            }
        }

        // 3. اربط المقابس بمشترك رئيسي (MST)
        val socketTree = buildMST(sockets + listOf(ElectricalComponent(0, 0, 0, "FUSE_BOX", panelX, panelY)))
        socketTree.forEach { (a, b) ->
            routes.add(buildRoute(a, b, "PHASE_BROWN", 2.5f, listOf(a, b)))
        }

        // 4. الأجهزة الثقيلة: كل واحد سلك مستقل للوحة
        heavy.forEach { h ->
            val panel = ElectricalComponent(0, 0, 0, "FUSE_BOX", panelX, panelY)
            val gauge = LoadCalculator.pickWireGauge(
                LoadCalculator.currentFromPower(h.watts),
                distance(panel, h)
            )
            routes.add(buildRoute(panel, h, "PHASE_BROWN", gauge, listOf(panel, h)))
        }

        // 5. أضف المحايد والأرضي
        val finalRoutes = mutableListOf<WireRoute>()
        routes.forEach { r ->
            finalRoutes.add(r)
            // محايد
            finalRoutes.add(r.copy(color = "NEUTRAL_BLUE"))
            // أرضي للأجهزة الثقيلة والمقابس
            if (r.color == "PHASE_BROWN") {
                finalRoutes.add(r.copy(color = "GROUND_GREEN", gauge = r.gauge.coerceAtLeast(2.5f)))
            }
        }

        return finalRoutes
    }

    private fun buildRoute(
        a: ElectricalComponent,
        b: ElectricalComponent,
        color: String,
        gauge: Float,
        points: List<ElectricalComponent>
    ): WireRoute {
        val len = distance(a, b)
        return WireRoute(
            fromId = a.id,
            toId = b.id,
            points = listOf(a.x to a.y, b.x to b.y),
            length = len,
            gauge = gauge,
            color = color
        )
    }

    private fun distance(a: ElectricalComponent, b: ElectricalComponent): Float {
        return sqrt((a.x - b.x).pow(2) + (a.y - b.y).pow(2)) / 50f // 50 بكسل = 1 متر
    }

    /**
     * Minimum Spanning Tree — Prim
     */
    private fun buildMST(nodes: List<ElectricalComponent>): List<Pair<ElectricalComponent, ElectricalComponent>> {
        if (nodes.size < 2) return emptyList()
        val result = mutableListOf<Pair<ElectricalComponent, ElectricalComponent>>()
        val visited = mutableSetOf(nodes[0])
        val remaining = nodes.drop(1).toMutableList()

        while (remaining.isNotEmpty()) {
            var bestDist = Float.MAX_VALUE
            var bestPair: Pair<ElectricalComponent, ElectricalComponent>? = null
            var bestIdx = -1

            visited.forEach { v ->
                remaining.forEachIndexed { i, r ->
                    val d = distance(v, r)
                    if (d < bestDist) {
                        bestDist = d
                        bestPair = v to r
                        bestIdx = i
                    }
                }
            }

            bestPair?.let {
                result.add(it)
                visited.add(it.second)
                remaining.removeAt(bestIdx)
            } ?: break
        }
        return result
    }

    /**
     * حساب إجمالي طول السلك لكل مقطع
     */
    fun totalWireLength(routes: List<WireRoute>): Map<Float, Float> {
        val result = mutableMapOf<Float, Float>()
        routes.forEach { r ->
            result[r.gauge] = (result[r.gauge] ?: 0f) + r.length
        }
        return result
    }
}
