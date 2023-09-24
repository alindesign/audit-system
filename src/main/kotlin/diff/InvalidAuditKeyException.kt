package diff

class InvalidAuditKeyException(auditName: String) :
    RuntimeException("No id property or @AuditKey annotated field found for $auditName")
