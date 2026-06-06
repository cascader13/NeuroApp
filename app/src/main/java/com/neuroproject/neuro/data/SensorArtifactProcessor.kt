package com.neuroproject.neuro.data

/**
 * Единая точка для будущей политики обработки артефактов.
 *
 * Сейчас класс намеренно работает в режиме no-op: данные не отбрасываются и не мутируются,
 * чтобы поток JNI -> Room оставался полным. Правила можно заменить здесь, не переписывая
 * JNI callbacks, RecordManager и DAO.
 */
class SensorArtifactProcessor {

    fun shouldStoreNfb(alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float): Boolean =
        listOf(alpha, beta, theta, delta, smr).all { it.isFinite() }

    fun shouldStoreEeg(channel1: Float, channel2: Float): Boolean =
        channel1.isFinite() && channel2.isFinite()

    fun shouldStoreArtifacts(
        artifactChannel1: Boolean,
        artifactChannel2: Boolean,
        qualityChannel1: Float,
        qualityChannel2: Float
    ): Boolean = qualityChannel1.isFinite() && qualityChannel2.isFinite()

    fun shouldStorePhysiological(values: List<Float>): Boolean = values.all { it.isFinite() }

    fun shouldStoreMems(values: List<Float>): Boolean = values.all { it.isFinite() }

    fun shouldStoreCardio(values: List<Float>): Boolean = values.all { it.isFinite() }

    fun shouldStoreProductivity(values: List<Float>): Boolean = values.all { it.isFinite() }

    fun shouldStoreEmotional(values: List<Float>): Boolean = values.all { it.isFinite() }
}
