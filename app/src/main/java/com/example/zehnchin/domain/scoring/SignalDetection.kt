package com.example.zehnchin.domain.scoring

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Signal detection measures for go/no-go style tasks.
 * d′ separates sensitivity from response bias, so a player who taps everything
 * cannot score well by accident.
 */
object SignalDetection {

    /**
     * d′ = z(hit rate) − z(false-alarm rate).
     * Uses the log-linear correction (add 0.5 to each cell) so rates of 0 or 1 stay finite.
     */
    fun dPrime(hits: Int, misses: Int, falseAlarms: Int, correctRejections: Int): Double {
        val hitRate = (hits + 0.5) / (hits + misses + 1.0)
        val falseAlarmRate = (falseAlarms + 0.5) / (falseAlarms + correctRejections + 1.0)
        return inverseNormal(hitRate) - inverseNormal(falseAlarmRate)
    }

    /** Coefficient of variation (sample SD / mean) of reaction times. Null when fewer than two values. */
    fun coefficientOfVariation(values: List<Long>): Double? {
        if (values.size < 2) return null
        val mean = values.average()
        if (mean == 0.0) return null
        val variance = values.sumOf { (it - mean) * (it - mean) } / (values.size - 1)
        return sqrt(variance) / mean
    }

    /**
     * Inverse of the standard normal CDF (Abramowitz & Stegun 26.2.23, absolute error < 4.5e-4).
     * Accurate enough for d′ at the precision the app reports.
     */
    internal fun inverseNormal(p: Double): Double {
        require(p > 0.0 && p < 1.0) { "p must be in (0, 1)" }
        val tail = if (p < 0.5) p else 1.0 - p
        val t = sqrt(-2.0 * ln(tail))
        val z = t - (2.515517 + 0.802853 * t + 0.010328 * t * t) /
            (1.0 + 1.432788 * t + 0.189269 * t * t + 0.001308 * t * t * t)
        return if (p < 0.5) -z else z
    }
}
