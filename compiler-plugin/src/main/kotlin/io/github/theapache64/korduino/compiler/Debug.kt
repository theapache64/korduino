package io.github.theapache64.korduino.compiler

import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Recursively extracts all properties and values from an object up to [depth] levels deep.
 *
 * @param depth How many levels deep to traverse nested objects (default: 1)
 * @param includePrivate Whether to include private properties (default: false)
 * @return A map of property names to their values (nested objects become nested maps)
 */
fun Any?.toPropertyMap(
    depth: Int = 1,
    includePrivate: Boolean = false
): Map<String, Any?> {
    if (this == null || depth < 1) return emptyMap()

    // Skip primitives, strings, and other "leaf" types
    if (this.isLeafType()) return mapOf("value" to this)

    // Handle collections/arrays specially
    if (this is Collection<*>) {
        return mapOf("items" to this.mapIndexed { index, item ->
            index.toString() to item.toPropertyMapInternal(depth - 1, includePrivate)
        }.toMap())
    }

    if (this is Map<*, *>) {
        return this.entries.associate { (k, v) ->
            k.toString() to v.toPropertyMapInternal(depth - 1, includePrivate)
        }
    }

    return this::class.memberProperties
        .filter { prop ->
            if (includePrivate) true
            else runCatching {
                prop.isAccessible = true
                true
            }.getOrDefault(true)
        }
        .associate { prop ->
            try {
                prop.isAccessible = true
                val value = prop.getter.call(this)
                prop.name to value.toPropertyMapInternal(depth - 1, includePrivate)
            } catch (e: Exception) {
                prop.name to "<error: ${e.message}>"
            }
        }
}

private fun Any?.toPropertyMapInternal(
    depth: Int,
    includePrivate: Boolean
): Any? {
    if (this == null) return null
    if (depth < 1 || this.isLeafType()) return this

    return when (this) {
        is Collection<*> -> this.map { it.toPropertyMapInternal(depth - 1, includePrivate) }
        is Map<*, *> -> this.entries.associate { (k, v) ->
            k.toString() to v.toPropertyMapInternal(depth - 1, includePrivate)
        }
        is Array<*> -> this.map { it.toPropertyMapInternal(depth - 1, includePrivate) }
        else -> this.toPropertyMap(depth, includePrivate)
    }
}

private fun Any.isLeafType(): Boolean = when (this) {
    is Number, is Boolean, is Char, is String, is Enum<*> -> true
    is ByteArray, is CharArray, is ShortArray, is IntArray,
    is LongArray, is FloatArray, is DoubleArray, is BooleanArray -> true
    else -> false
}