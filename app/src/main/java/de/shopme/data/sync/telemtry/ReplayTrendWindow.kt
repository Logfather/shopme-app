package de.shopme.data.sync.telemetry

import de.shopme.data.sync.telemtry.ReplayOutcome

data class ReplayTrendWindow(

    val recentOutcomes:
    List<ReplayOutcome> = emptyList(),

    val recentDurationsMs:
    List<Long> = emptyList(),

    val recentProcessedEntries:
    List<Int> = emptyList()
)