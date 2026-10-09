package com.imadkrouni.barq.engine

import kotlin.math.ceil
import kotlin.math.sqrt

object LoadCalculator {

    // اختيار القاطع حسب التيار (مع هامش 25%)
    fun pickBreaker(currentAmp: Float): Int {
        val withMargin = currentAmp * 1.25f
        val standards = listOf(6, 10, 16, 20, 25, 32, 40, 50, 63, 80, 100)
        return standards.firstOrNull { it >= withMargin } ?: 125
    }

    // اختيار مقطع السلك حسب التيار والمسافة
    fun pickWireGauge(currentAmp: Float, lengthMeters: Float, copper: Boolean = true): Float {
        val table = listOf(
            1.5f to 16f, 2.5f to 25f, 4f to 32f, 6f to 40f,
            10f to 63f, 16f to 80f, 25f to 100f, 35f to 125f
        )
        val distanceFactor = when {
            lengthMeters > 50 -> 1.4f
            lengthMeters > 30 -> 1.25f
            lengthMeters > 15 -> 1.15f
            else -> 1.0f
        }
        val metalFactor = if (copper) 1.0f else 1.6f
        val neededAmp = currentAmp * distanceFactor * metalFactor
        return table.firstOrNull { it.second >= neededAmp }?.first ?: 35f
    }

    // انخفاض الجهد (فولت)
    fun voltageDrop(currentAmp: Float, lengthM: Float, gauge: Float, copper: Boolean = true): Float {
        val rho = if (copper) 0.0172f else 0.028f
        return (2 * rho * lengthM * currentAmp) / gauge
    }

    // حساب القدرة
    fun power(voltage: Float, current: Float): Float = voltage * current

    // تيار من قدرة
    fun currentFromPower(watts: Float, voltage: Float = 220f): Float = watts / voltage

    // إجمالي أحمال المشروع
    fun totalLoad(componentsWatts: List<Float>): Float = componentsWatts.sum()
}
