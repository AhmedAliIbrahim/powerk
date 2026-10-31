package com.powerk.app

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class MeasurementsTest {
    @Test fun smallReadingsKeepTheirPrecisionAndUnknownReadingsStayUnknown() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            assertEquals("0.01", measurement(0.01))
            assertEquals("0.001", measurement(0.001, 3))
            assertEquals("0.02", measurement(0.02))
            assertEquals("—", measurement(null))
            assertEquals("—", measurement(Double.NaN))
            assertEquals("—", measurement(Double.POSITIVE_INFINITY))
            assertEquals("0", measurement(0.0))
        } finally { Locale.setDefault(previous) }
    }

    @Test fun disconnectedDevicesAndFailedPollingAreExcludedFromLiveTotals() {
        val online = Strip("AABBCCDDEEFF", "Office", "lgutap", "1", true, true, .01, .001,
            null, null, null, listOf(Outlet(1, true, .01, .001, 25), Outlet(2, false, 0.0, 0.0, 25)))
        val offline = online.copy(mac = "112233445566", online = false, powerW = 999.0)
        assertEquals(LiveSummary(.01, 1, 1, 2), liveSummary(listOf(online, offline), true))
        assertEquals(LiveSummary(null, 0, 0, 0), liveSummary(listOf(online, offline), false))
        assertEquals(LiveSummary(null, 0, 0, 0), liveSummary(listOf(offline), true))
        assertEquals(LiveSummary(null, 0, 0, 0), liveSummary(emptyList(), true))
    }
}
