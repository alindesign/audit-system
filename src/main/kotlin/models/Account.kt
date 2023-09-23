package models


data class Account(
    val id: Int,
    val person: Person,
    val subscription: Subscription?,
    val vehicles: List<Vehicle>,
    val services: List<String>,
    val metadata: Map<String, Metadata>? = null,
    val notes: Map<String, String>? = null
)
