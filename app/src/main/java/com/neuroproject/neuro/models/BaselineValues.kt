package com.neuroproject.neuro.models

data class BaselineValues(val alpha: Float, val alphaGravity: Float, val beta: Float, val betaGravity: Float){
    fun isValid():Boolean{
        return alpha != 0f && alphaGravity != 0f && beta != 0f && betaGravity != 0f
    }
}
