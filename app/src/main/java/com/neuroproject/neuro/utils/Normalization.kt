package com.neuroproject.neuro.utils

import kotlin.math.exp

/**
 * Утилита нормализации значений датчиков.
 *
 * Предоставляет методы приведения сырых данных к диапазону [0, 1]
 * с учётом базовых значений и инверсии для обратных шкал.
 */
object Normalization {

    fun minMaxClamped(value: Float, min: Float = 0f, max: Float = 100f) : Float{
        return ((value - min)/(max - min)).coerceIn(0f, 1f) // coerceIn определяет границы, и при выходе за них попросту подставляет минимум или максимум
    }

    fun invertClamped(value:Float, min: Float = 0f, max: Float = 100f) : Float{
        return 1f - minMaxClamped(value, min, max)
    }

    fun normalizeWithBaseline(value: Float, baseline: Float, min: Float = 0f, max: Float? = null) : Float{
        var norm: Float
        if (value <= baseline){
            norm = (0.5f * (value - min) / (baseline - min)).coerceIn(0f,0.5f)
        }else{
            if(max == null){
                var ratio: Float = (value - baseline) / baseline
                norm = (0.5f + 0.5f * (1 - exp(-ratio))).coerceIn(0.5f, 1f)
            }else{
                norm = (0.5f + 0.5f * (value - baseline) / (max - baseline)).coerceIn(0.5f, 1f)
            }
        }
        return norm
    }

    fun invertWithBaseline(value:Float, baseline: Float, min: Float = 0f, max: Float? = null) : Float{
        return 1 - normalizeWithBaseline(value, baseline, min, max)
    }

    fun normalizeProdFatique(value: Float, baseline: Float) = normalizeWithBaseline(value, baseline)
    fun normalizeConcentration(value: Float, baseline: Float) = invertWithBaseline(value, baseline)
    fun normalizeProductivity(value: Float, baseline: Float) = invertWithBaseline(value, baseline)
    fun normalizeCognitiveLoad(value: Float) = minMaxClamped(value)
    fun normalizePhysFatique(value: Float): Float{
        return value
    }
    fun normalizeStress(value: Float): Float{
        return value
    }
    fun normalizeRelax(value: Float): Float{
        return 1 - value
    }
    fun normalizeInvolvement(value: Float): Float {
        return 1 - value
    }
    fun normalizeRelaxation(value: Float) = invertClamped(value)
    fun normalizeSelfControl(value: Float) = invertClamped(value)
    fun normalizeCognitiveControl(value: Float) = invertClamped(value)
}