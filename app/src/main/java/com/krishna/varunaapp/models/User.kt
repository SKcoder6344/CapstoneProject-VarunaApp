package com.krishna.varunaapp.models

data class User(
    val username: String = "",
    val mobile: String = "",
    val email: String = "",
    val role: String = "GeneralUser",
    val state: String = "",
    val village: String = "",
    val pincode: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "username" to username,
        "mobile" to mobile,
        "email" to email,
        "role" to role,
        "state" to state,
        "village" to village,
        "pincode" to pincode
    )
}
