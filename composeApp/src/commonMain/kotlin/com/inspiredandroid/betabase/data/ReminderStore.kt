package com.inspiredandroid.betabase.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ReminderRecord(
    val eventId: String,
    val title: String,
    val body: String,
    val triggerEpochMillis: Long,
)

interface ReminderStore {
    fun load(): Set<ReminderRecord>
    fun save(records: Set<ReminderRecord>)
    fun contains(eventId: String): Boolean = load().any { it.eventId == eventId }
    fun upsert(record: ReminderRecord) {
        val next = load().filterNot { it.eventId == record.eventId }.toMutableSet()
        next.add(record)
        save(next)
    }

    fun remove(eventId: String) {
        save(load().filterNot { it.eventId == eventId }.toSet())
    }

    /** Drop reminders whose trigger time has already passed. Returns the pruned set. */
    fun prunePast(nowEpochMillis: Long): Set<ReminderRecord> {
        val current = load()
        val kept = current.filter { it.triggerEpochMillis > nowEpochMillis }.toSet()
        if (kept.size != current.size) save(kept)
        return kept
    }
}

/** Platform-backed reminder persistence via the shared [StringStore]. */
fun createReminderStore(store: StringStore = createStringStore()): ReminderStore = ReminderStore(
    get = store::get,
    put = store::put,
)

/**
 * Builds a [ReminderStore] over any string key-value store.
 * Platforms only supply get/put; encode/decode stays shared.
 */
fun ReminderStore(
    get: (key: String) -> String?,
    put: (key: String, value: String) -> Unit,
    key: String = PrefKeys.REMINDERS,
): ReminderStore = object : ReminderStore {
    override fun load(): Set<ReminderRecord> =
        (get(key) ?: get(PrefKeys.LEGACY_REMINDERS)?.also { put(key, it) })
            ?.let(::decodeReminders)
            .orEmpty()

    override fun save(records: Set<ReminderRecord>) = put(key, encodeReminders(records))
}

private val json = Json { ignoreUnknownKeys = true }

fun encodeReminders(records: Set<ReminderRecord>): String =
    json.encodeToString(records.toList())

fun decodeReminders(raw: String): Set<ReminderRecord> = runCatching {
    json.decodeFromString<List<ReminderRecord>>(raw).toSet()
}.getOrDefault(emptySet())
