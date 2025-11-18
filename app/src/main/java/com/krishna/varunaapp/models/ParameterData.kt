package com.krishna.varunaapp.models

data class ParameterData(
    val parameter: String = "",
    val samples: List<Float> = emptyList(),
    val epaStandard: String = ""
)
