package com.neuroproject.neuro.domain.model

/**
 * Базовые физиологические значения (альфа и бета ритмы) для калибровки.
 *
 * @property alpha альфа-активность
 * @property alphaGravity гравитационная составляющая альфа-ритма
 * @property beta бета-активность
 * @property betaGravity гравитационная составляющая бета-ритма
 */
data class BaselineValues(val alpha: Float, val alphaGravity: Float, val beta: Float, val betaGravity: Float){
    fun isValid():Boolean{
        return alpha != 0f && alphaGravity != 0f && beta != 0f && betaGravity != 0f
    }
}
