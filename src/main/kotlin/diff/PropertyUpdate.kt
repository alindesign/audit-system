package diff


data class PropertyUpdate<T>(val property: String, val previous: T?, val current: T?) : ChangeType()
