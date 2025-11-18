package com.krishna.varunaapp.models

data class WaterTestTable(
    val createdAt: Long = System.currentTimeMillis(),
    val rows: List<WaterParameter> = emptyList()
)
