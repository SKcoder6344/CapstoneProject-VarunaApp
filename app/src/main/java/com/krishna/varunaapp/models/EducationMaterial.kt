package com.krishna.varunaapp.models

data class EducationMaterial(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val fileUrl: String = "",
    val fileType: String = "",
    val uploadedAt: Long = 0L,
    val uploadedBy: String = ""
)
