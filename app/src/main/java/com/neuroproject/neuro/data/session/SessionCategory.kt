package com.neuroproject.neuro.data.session

/**
 * Обратная совместимость для старых импортов data.session.SessionCategory.
 * Реальный enum находится в domain.model, поэтому data-слой больше не протекает в domain/UI.
 */
typealias SessionCategory = com.neuroproject.neuro.domain.model.SessionCategory
