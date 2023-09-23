package models

import diff.AuditKey

data class Vehicle(
    @AuditKey
    val id: Int,
    val displayName: String
)
