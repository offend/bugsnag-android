@file:Suppress("UNCHECKED_CAST")

package com.bugsnag.android

import java.io.IOException
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Observable
import java.util.concurrent.ConcurrentHashMap

/**
 * A container for additional diagnostic information you'd like to send with
 * every error report.
 *
 *
 * Diagnostic information is presented on your Bugsnag dashboard in tabs.
 */
class Metadata @JvmOverloads constructor(map: Map<String, Any> = ConcurrentHashMap()) :
    Observable(), JsonStream.Streamable, MetadataAware {

    private val store: MutableMap<String, Any> = ConcurrentHashMap(map)
    internal val jsonStreamer = ObjectJsonStreamer()
    val redactedKeys: Set<String> = jsonStreamer.redactedKeys

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        jsonStreamer.objectToStream(store, writer, true)
    }

    override fun addMetadata(section: String, value: Map<String, Any?>) {
        value.entries.forEach {
            addMetadata(section, it.key, it.value)
        }
    }
    override fun clearMetadata(section: String) = clearMetadata(section, null)
    override fun getMetadata(section: String) = getMetadata(section, null)

    override fun addMetadata(section: String, key: String?, value: Any?) {
        if (value == null) {
            clearMetadata(section, key)
        } else {
            val tab = getOrAddSection(section)

            if (key == null) {
                store[section] = value
            } else {
                tab[key] = value
            }

            setChanged()
            notifyObservers(
                NativeInterface.Message(
                    NativeInterface.MessageType.ADD_METADATA,
                    Arrays.asList<Any>(section, key, value)
                )
            )
        }
    }

    override fun clearMetadata(section: String, key: String?) {
        setChanged()

        if (key == null) {
            store.remove(section)
            notifyObservers(
                NativeInterface.Message(NativeInterface.MessageType.CLEAR_METADATA_TAB, section)
            )
        } else {
            val tab = store[section]

            if (tab is MutableMap<*, *>) {
                tab.remove(key)
            }
            notifyObservers(
                NativeInterface.Message(
                    NativeInterface.MessageType.REMOVE_METADATA, listOf(section, key)
                )
            )
        }
    }

    override fun getMetadata(section: String, key: String?): Any? {
        val tab = store[section]

        return when {
            tab is Map<*, *> && key != null -> {
                (tab as Map<String, Any>?)!![key]
            }
            else -> tab
        }
    }

    private fun getOrAddSection(section: String): MutableMap<String, Any> {
        var tab = store[section]

        if (tab !is MutableMap<*, *>) {
            tab = ConcurrentHashMap<Any, Any>()
            store[section] = tab
        }

        return (tab as MutableMap<String, Any>?)!!
    }

    fun toMap(): Map<String, Any> = HashMap(store)

    fun setRedactedKeys(redactedKeys: Collection<String>) {
        val data = HashSet(redactedKeys)
        jsonStreamer.redactedKeys.clear()
        jsonStreamer.redactedKeys.addAll(data)
    }

    companion object {
        fun merge(vararg data: Metadata): Metadata {
            val stores = data.map { it.toMap() }
            val filters = data.flatMap { it.jsonStreamer.redactedKeys }
            val newMeta = Metadata(mergeMaps(stores))
            newMeta.setRedactedKeys(filters.toSet())
            return newMeta
        }

        private fun mergeMaps(data: List<Map<String, Any>>): Map<String, Any> {
            val keys = data.flatMap { it.keys }.toSet()
            val result = ConcurrentHashMap<String, Any>()

            for (map in data) {
                for (key in keys) {
                    getMergeValue(result, key, map)
                }
            }
            return result
        }

        private fun getMergeValue(
            result: ConcurrentHashMap<String, Any>,
            key: String,
            map: Map<String, Any>
        ) {
            val baseValue = result[key]
            val overridesValue = map[key]

            if (overridesValue != null) {
                if (baseValue is Map<*, *> && overridesValue is Map<*, *>) {
                    // Both original and overrides are Maps, go deeper
                    val first = baseValue as Map<String, Any>?
                    val second = overridesValue as Map<String, Any>?
                    result[key] = mergeMaps(listOf(first!!, second!!))
                } else {
                    result[key] = overridesValue
                }
            } else {
                if (baseValue != null) { // No collision, just use base value
                    result[key] = baseValue
                }
            }
        }
    }
}