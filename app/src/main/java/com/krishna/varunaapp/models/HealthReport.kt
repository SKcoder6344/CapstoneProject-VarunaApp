package com.krishna.varunaapp.models

data class HealthReport(
    val id: String = "",
    val patientName: String = "",
    val age: Int = 0,
    val gender: String = "",
    val symptoms: List<String> = listOf(),
    val symptomStartDate: Long = 0L,
    val severity: String = "Mild", // Mild, Moderate, Severe
    val waterSource: String = "",
    val additionalNotes: String = "",
    val status: String = "Not Cured", // "Cured" or "Not Cured"
    val createdAt: Long = System.currentTimeMillis(),
    val villageName: String = "",
    val reportedBy: String = "" // User ID who created the report
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", 0, "", listOf(), 0L, "Mild", "", "", "Not Cured", 0L, "", "")
}