package com.powerk.app

import java.text.NumberFormat

/** Preserve small nonzero API readings without unhelpful trailing zeros. */
fun measurement(value: Double?, digits: Int = 2): String {
    if (value == null || !value.isFinite()) return "—"
    return NumberFormat.getNumberInstance().apply { maximumFractionDigits = digits; minimumFractionDigits = 0 }.format(value)
}

data class LiveSummary(val powerW: Double?, val reporting: Int, val active: Int, val outlets: Int)

/** Failed polling and disconnected strips must never contribute stale readings to live totals. */
fun liveSummary(strips: List<Strip>, healthy: Boolean): LiveSummary {
    val reporting = if (healthy) strips.filter { it.online } else emptyList()
    return LiveSummary(
        powerW = reporting.takeIf { it.isNotEmpty() }?.sumOf { it.powerW },
        reporting = reporting.size,
        active = reporting.sumOf { strip -> strip.outlets.count { it.on } },
        outlets = reporting.sumOf { it.outlets.size },
    )
}
