package de.shopme.data.sync.telemetry

import de.shopme.data.sync.telemtry.ReplayOutcome

class RuntimeTrendAnalyzer(

    private val maxWindowSize: Int = 10
) {

    private val outcomes =
        mutableListOf<ReplayOutcome>()

    private val durations =
        mutableListOf<Long>()

    private val processedEntries =
        mutableListOf<Int>()

    fun recordReplay(

        outcome: ReplayOutcome,

        durationMs: Long,

        processedCount: Int
    ) {

        outcomes += outcome
        durations += durationMs
        processedEntries += processedCount

        trim()
    }

    fun snapshot():
            RuntimeTrendSnapshot {

        if (outcomes.isEmpty()) {

            return RuntimeTrendSnapshot(

                recentReplaySuccessRate = 1.0,

                recentReplayDegradationRate = 0.0,

                recentReplayFailureRate = 0.0,

                recentAverageReplayDurationMs = 0.0,

                recentAverageProcessedEntries = 0.0
            )
        }

        val total =
            outcomes.size.toDouble()

        return RuntimeTrendSnapshot(

            recentReplaySuccessRate =

                outcomes.count {
                    it == ReplayOutcome.SUCCESS
                } / total,

            recentReplayDegradationRate =

                outcomes.count {
                    it == ReplayOutcome.DEGRADED_SUCCESS
                } / total,

            recentReplayFailureRate =

                outcomes.count {
                    it == ReplayOutcome.FAILURE
                } / total,

            recentAverageReplayDurationMs =

                durations.average(),

            recentAverageProcessedEntries =

                processedEntries.average()
        )
    }

    private fun trim() {

        while (outcomes.size > maxWindowSize) {

            outcomes.removeAt(0)
        }

        while (durations.size > maxWindowSize) {

            durations.removeAt(0)
        }

        while (
            processedEntries.size >
            maxWindowSize
        ) {

            processedEntries.removeAt(0)
        }
    }
}