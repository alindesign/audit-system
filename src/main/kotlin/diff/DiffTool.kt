package diff

import kotlin.reflect.full.memberProperties


class DiffTool {
    fun <T : Any> diff(previous: T?, current: T?): List<ChangeType> {
        val changes = mutableListOf<ChangeType>()
        compareObjects("", previous, current, changes)
        return changes
    }

    private fun <T> compareObjects(path: String, previous: T?, current: T?, changes: MutableList<ChangeType>) {
        // if values are the same we don't check we just return
        if (previous == current) {
            return
        }

        // Compare the objects based on their types
        when {
            // If any of the values is a list, we'll consider a compare between lists
            // even if the other value is null.
            // The type will be enforced by the T type from the function signature, meaning that
            // the type can be either a List<*> or a null.
            previous is List<*> || current is List<*> -> {
                val previousList: List<*> = if (previous == null) listOf<Any>() else previous as List<*>
                val currentList: List<*> = if (current == null) listOf<Any>() else current as List<*>

                compareLists(path, previousList, currentList, changes)
            }

            // If any of the values is a map, we'll consider a compare between maps, like the list case,
            // the values could be a map or a null as the type is enforced by the T type from the function signature.
            previous is Map<*, *> || current is Map<*, *> -> {
                val previousMap: Map<*, *> = if (previous == null) mapOf<Any, Any>() else previous as Map<*, *>
                val currentMap: Map<*, *> = if (current == null) mapOf<Any, Any>() else current as Map<*, *>

                compareMap(path, previousMap, currentMap, changes)
            }

            // In case any of the values is a primitive, we'll compare the values directly
            isPrimitive(previous) || isPrimitive(current) -> {
                comparePrimitive(path, previous, current, changes)
            }

            // If none of the above cases are true, we'll consider the values as objects and compare their properties.
            else -> {
                compareProperties(path, previous, current, changes)
            }
        }
    }

    private fun <T> comparePrimitive(path: String, previous: T?, current: T?, changes: MutableList<ChangeType>) {
        if (previous != current) {
            changes.add(PropertyUpdate(path, previous, current))
        }
    }

    private fun compareMap(path: String, previous: Map<*, *>, current: Map<*, *>, changes: MutableList<ChangeType>) {
        // break out if the maps are the same
        if (previous == current) {
            return
        }

        // get all keys from both maps
        val allKeys = previous.keys.union(current.keys)

        // loop through all keys and compare the values
        allKeys.forEach { key ->
            val newPath = if (path.isEmpty()) key.toString() else "$path.$key"

            // compare the keys from both maps
            compareObjects(newPath, previous[key], current[key], changes)
        }
    }

    private fun compareLists(path: String, previous: List<*>, current: List<*>, changes: MutableList<ChangeType>) {
        // break out if the lists are the same
        if (previous == current) {
            return
        }

        val isPreviousPrimitiveList = previous.all { isPrimitive(it) }
        val isCurrentPrimitiveList = current.all { isPrimitive(it) }
        val isPrimitiveList = isPreviousPrimitiveList && isCurrentPrimitiveList

        // throw an exception if we have a primitive list and a non-primitive list
        if (isPreviousPrimitiveList && !isCurrentPrimitiveList || !isPreviousPrimitiveList && isCurrentPrimitiveList) {
            throw MixedListValuesException()
        }

        if (isPrimitiveList) {
            comparePrimitiveList(path, previous, current, changes)
        } else {
            compareObjectList(path, previous, current, changes)
        }
    }

    private fun compareObjectList(path: String, previous: List<*>, current: List<*>, changes: MutableList<ChangeType>) {
        // construct a map of the previous and current lists by id
        val previousById = previous.associateBy { getId(it) }
        val currentById = current.associateBy { getId(it) }

        // compare the maps
        compareMap(path, previousById, currentById, changes)
    }

    private fun comparePrimitiveList(
        path: String,
        previous: List<*>,
        current: List<*>,
        changes: MutableList<ChangeType>
    ) {
        val added = current.filter { it !in previous }
        val removed = previous.filter { it !in current }

        if (added.isNotEmpty() || removed.isNotEmpty()) {
            changes.add(ListUpdate(path, removed, added))
        }
    }

    private fun <T> compareProperties(path: String, previous: T?, current: T?, changes: MutableList<ChangeType>) {
        // break out if the objects are the same
        if (previous == current) {
            return
        }

        // get all properties from both objects
        val previousProps = previous?.let { it::class.memberProperties } ?: emptyList()
        val currentProps = current?.let { it::class.memberProperties } ?: emptyList()
        val props = (previousProps + currentProps).toSet()

        // loop through all properties and compare the values
        props.forEach { prop ->
            val name = prop.name
            val newPath = if (path.isEmpty()) name else "$path.$name"
            val nextPrevious = previousProps.find { it.name == name }?.getter?.call(previous)
            val nextCurrent = currentProps.find { it.name == name }?.getter?.call(current)

            compareObjects(newPath, nextPrevious, nextCurrent, changes)
        }
    }

    private fun getId(value: Any?): Any? {
        // make sure we have a value that's not null
        if (value == null) {
            throw InvalidAuditKeyException("null")
        }

        // grab all fields that are annotated with @AuditKey
        val propsByAnnotation = value::class.memberProperties.filter {
            it.annotations.any { annotation -> annotation is AuditKey }
        }

        // throw an exception if we have more than one @AuditKey annotated field
        if (propsByAnnotation.size > 1) {
            throw MultipleAuditKeyException(value::class.simpleName ?: "Unknown")
        }

        // if we have an id field, return it
        val propsById = value::class.memberProperties.find { it.name == "id" }
        if (propsById != null) {
            return propsById.getter.call(value)
        }

        // otherwise, if we have an @AuditKey annotated field, return it
        if (propsByAnnotation.isNotEmpty()) {
            val prop = propsByAnnotation.first()
            return prop.getter.call(value)
        }

        // At this point if we didn't find an id or @AuditKey annotated
        // field, we throw an exception no matter what
        throw InvalidAuditKeyException(value::class.simpleName ?: "Unknown")
    }

    private fun <T> isPrimitive(value: T?): Boolean {
        return when (value) {
            is String -> true
            is Number -> true
            is Boolean -> true
            is Char -> true
            else -> {
                value != null && value.javaClass.isPrimitive
            }
        }
    }
}
