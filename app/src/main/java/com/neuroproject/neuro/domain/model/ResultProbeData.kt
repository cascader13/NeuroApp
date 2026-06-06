package com.neuroproject.neuro.domain.model

import java.util.Date

data class ResultProbeData(var HR: Float,
                           var AlphaBaseline: Float,
                           val Relax: Float,
                           val Fatigue: Float,
                           val Involvement: Float,
                           val Stress: Float,
                           val Alpha: Float,
                           val Beta: Float,
                           val Theta: Float,
                           var Smr: Float,
                           var Delta: Float,
                           val date: Date
    )
