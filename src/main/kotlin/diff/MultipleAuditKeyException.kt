package diff

class MultipleAuditKeyException(auditName: String) : RuntimeException("Audit $auditName has more than one field annotated with @AuditKey")
