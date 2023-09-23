package diff


data class ListUpdate<T>(val property: String, val removed: List<T?>, val added: List<T?>) : ChangeType()
