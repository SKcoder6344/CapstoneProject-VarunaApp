package com.krishna.varunaapp.models

data class HelpRequest(
    var id: String? = null,
    val userId: String? = null,
    val message: String? = null,
    val timestamp: com.google.firebase.Timestamp? = null,
    val status: String? = "Pending"
)
