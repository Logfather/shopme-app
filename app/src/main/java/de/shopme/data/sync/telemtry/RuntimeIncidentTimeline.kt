package de.shopme.data.sync.telemetry

class RuntimeIncidentTimeline(

    private val maxEntries: Int = 50
) {

    private val events =
        mutableListOf<RuntimeTimelineEvent>()

    fun record(
        event: RuntimeTimelineEvent
    ) {

        events += event

        trim()
    }

    fun snapshot():
            List<RuntimeTimelineEvent> {

        return events.toList()
    }

    private fun trim() {

        while (events.size > maxEntries) {

            events.removeAt(0)
        }
    }
}