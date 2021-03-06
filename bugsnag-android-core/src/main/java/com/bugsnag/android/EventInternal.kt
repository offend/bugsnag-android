package com.bugsnag.android

import java.io.IOException

internal class EventInternal @JvmOverloads internal constructor(
    val originalError: Throwable? = null,
    config: ImmutableConfig,
    private var severityReason: SeverityReason,
    data: Metadata = Metadata()
) : JsonStream.Streamable, MetadataAware, UserAware {

    val metadata: Metadata = data.copy()
    private val discardClasses: Set<String> = config.discardClasses.toSet()

    @JvmField
    internal var session: Session? = null

    var severity: Severity
        get() = severityReason.currentSeverity
        set(value) {
            severityReason.currentSeverity = value
        }

    var apiKey: String = config.apiKey
    lateinit var app: AppWithState
    lateinit var device: DeviceWithState
    var breadcrumbs: MutableList<Breadcrumb> = mutableListOf()
    var unhandled: Boolean
        get() = severityReason.unhandled
        set(value) {
            severityReason.unhandled = value
        }
    val unhandledOverridden: Boolean
        get() = severityReason.unhandledOverridden

    val originalUnhandled: Boolean
        get() = severityReason.originalUnhandled

    var errors: MutableList<Error> = when (originalError) {
        null -> mutableListOf()
        else -> Error.createError(originalError, config.projectPackages, config.logger)
    }

    var threads: MutableList<Thread> = ThreadState(originalError, unhandled, config).threads
    var groupingHash: String? = null
    var context: String? = null

    /**
     * @return user information associated with this Event
     */
    internal var _user = User(null, null, null)

    protected fun shouldDiscardClass(): Boolean {
        return when {
            errors.isEmpty() -> true
            else -> errors.any { discardClasses.contains(it.errorClass) }
        }
    }

    protected fun isAnr(event: Event): Boolean {
        val errors = event.errors
        var errorClass: String? = null
        if (errors.isNotEmpty()) {
            val error = errors[0]
            errorClass = error.errorClass
        }
        return "ANR" == errorClass
    }


    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        // Write error basics
        writer.beginObject()
        writer.name("context").value(context)
        writer.name("metaData").value(metadata)

        writer.name("severity").value(severity)
        writer.name("severityReason").value(severityReason)
        writer.name("unhandled").value(severityReason.unhandled)

        // Write exception info
        writer.name("exceptions")
        writer.beginArray()
        errors.forEach { writer.value(it) }
        writer.endArray()

        // Write user info
        writer.name("user").value(_user)

        // Write diagnostics
        writer.name("app").value(app)
        writer.name("device").value(device)
        writer.name("breadcrumbs").value(breadcrumbs)
        writer.name("groupingHash").value(groupingHash)

        writer.name("threads")
        writer.beginArray()
        threads.forEach { writer.value(it) }
        writer.endArray()

        if (session != null) {
            val copy = Session.copySession(session)
            writer.name("session").beginObject()
            writer.name("id").value(copy.id)
            writer.name("startedAt").value(DateUtils.toIso8601(copy.startedAt))
            writer.name("events").beginObject()
            writer.name("handled").value(copy.handledCount.toLong())
            writer.name("unhandled").value(copy.unhandledCount.toLong())
            writer.endObject()
            writer.endObject()
        }

        writer.endObject()
    }

    protected fun updateSeverityInternal(severity: Severity) {
        severityReason = SeverityReason.newInstance(severityReason.severityReasonType,
            severity, severityReason.attributeValue)
        this.severity = severity
    }

    fun getSeverityReasonType(): String = severityReason.severityReasonType

    override fun setUser(id: String?, email: String?, name: String?) {
        _user = User(id, email, name)
    }

    override fun getUser() = _user

    override fun addMetadata(section: String, value: Map<String, Any?>) = metadata.addMetadata(section, value)


    override fun addMetadata(section: String, key: String, value: Any?) =
        metadata.addMetadata(section, key, value)


    override fun clearMetadata(section: String) = metadata.clearMetadata(section)


    override fun clearMetadata(section: String, key: String) = metadata.clearMetadata(section, key)


    override fun getMetadata(section: String) = metadata.getMetadata(section)


    override fun getMetadata(section: String, key: String) = metadata.getMetadata(section, key)
}
