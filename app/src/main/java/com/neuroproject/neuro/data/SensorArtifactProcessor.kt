package com.neuroproject.neuro.data

/**
 * Единая точка для будущей политики обработки артефактов.
 *
 * Сейчас класс намеренно работает в режиме no-op: данные не отбрасываются и не мутируются,
 * чтобы поток JNI -> Room оставался полным. Правила можно заменить здесь, не переписывая
 * JNI callbacks, RecordManager и DAO.
 *
 * Проверяет корректность данных перед сохранением (отсутствие NaN, Infinity).
 */
class SensorArtifactProcessor {

    /**
     * Проверка, следует ли сохранять NFB данные.
     *
     * @return true если все значения конечны (не NaN, не Infinity)
     */
    fun shouldStoreNfb(alpha: Float, beta: Float, theta: Float, delta: Float, smr: Float): Boolean =
        listOf(alpha, beta, theta, delta, smr).all { it.isFinite() }

    /**
     * Проверка, следует ли сохранять ЭЭГ данные.
     *
     * @return true если оба канала конечны
     */
    fun shouldStoreEeg(channel1: Float, channel2: Float): Boolean =
        channel1.isFinite() && channel2.isFinite()

    /**
     * Проверка, следует ли сохранять данные об артефактах ЭЭГ.
     *
     * @return true если значения качества конечны
     */
    fun shouldStoreArtifacts(
        artifactChannel1: Boolean,
        artifactChannel2: Boolean,
        qualityChannel1: Float,
        qualityChannel2: Float
    ): Boolean = qualityChannel1.isFinite() && qualityChannel2.isFinite()

    /**
     * Проверка, следует ли сохранять физиологические данные.
     *
     * @return true если все значения конечны
     */
    fun shouldStorePhysiological(values: List<Float>): Boolean = values.all { it.isFinite() }

    /**
     * Проверка, следует ли сохранять MEMS данные.
     *
     * @return true если все значения конечны
     */
    fun shouldStoreMems(values: List<Float>): Boolean = values.all { it.isFinite() }

    /**
     * Проверка, следует ли сохранять кардио данные.
     *
     * @return true если все значения конечны
     */
    fun shouldStoreCardio(values: List<Float>): Boolean = values.all { it.isFinite() }

    /**
     * Проверка, следует ли сохранять данные продуктивности.
     *
     * @return true если все значения конечны
     */
    fun shouldStoreProductivity(values: List<Float>): Boolean = values.all { it.isFinite() }

    /**
     * Проверка, следует ли сохранять эмоциональные данные.
     *
     * @return true если все значения конечны
     */
    fun shouldStoreEmotional(values: List<Float>): Boolean = values.all { it.isFinite() }
}
