package com.krishna.varunaapp.models

data class Alert(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val severity: String = "",
    val createdAt: Long = 0L,
    val createdBy: String = ""
)
