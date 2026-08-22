package com.neuroproject.neuro.domain.model

/**
 * Физиологические базовые значения
 *
 * @property time временная метка
 * @property alpha альфа-активность
 * @property beta бета-активность
 * @property alphaGravity гравитационная составляющая альфа-ритма
 * @property betaGravity гравитационная составляющая бета-ритма
 * @property concentration уровень концентрации
 */
data class PhysiologicalBaselineSample(
    override val timestamp: Long = 0,
    override val sessionId: String = "",
    override val userId: String = "",
    override val expeditionId: String = "",
    val alpha: Float = 1f,
    val beta: Float = 1f,
    val alphaGravity: Float = 1f,
    val betaGravity: Float = 1f,
    val concentration: Float = 1f
): SensorSample()
