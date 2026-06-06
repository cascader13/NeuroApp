package com.neuroproject.neuro.domain.model


/**
 * Перечисление этапов калибровки устройства Capsule
 *
 * Определяет все возможные состояния процесса калибровки нейро-гарнитуры,
 * от начальной настройки до финальных этапов физиологической регистрации.
 *
 * @property value числовое представление этапа, используемое в нативном коде
 */
enum class CalibrationStage(val value: Int) {
    /** Неизвестное состояние калибратора */
    CALIBRATOR_UNKNOWN_STAGE(-2),
    /** Калибратор готов к работе */
    CALIBRATOR_READY_STAGE(-1),
    /** Первый этап калибровки */
    CALIBRATOR_STAGE1(0),
    /** Второй этап калибровки */
    CALIBRATOR_STAGE2(1),
    /** Третий этап калибровки */
    CALIBRATOR_STAGE3(2),
    /** Четвертый этап калибровки */
    CALIBRATOR_STAGE4(3),
    /** Этап инициализации физиологических измерений */
    PHYSIO_INIT_STAGE(4),
    /** Этап установления физиологического базового уровня */
    PHYSIO_BASELINE_STAGE(5),
    /** Ошибка калибратора */
    CALIBRATOR_ERROR_STAGE(6);

    companion object {
        /**
         * Преобразует числовое значение в соответствующее перечисление
         *
         * @param value числовое значение этапа
         * @return соответствующий этап калибровки, или [CALIBRATOR_UNKNOWN_STAGE] если значение не найдено
         */
        fun fromInt(value: Int) =
            CalibrationStage.entries.firstOrNull { it.value == value } ?: CALIBRATOR_UNKNOWN_STAGE
    }
}