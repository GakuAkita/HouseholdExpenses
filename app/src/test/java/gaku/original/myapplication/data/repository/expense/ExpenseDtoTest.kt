package gaku.original.myapplication.data.repository.expense

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ExpenseDtoTest {

    @Test
    fun toDatetimeString_alwaysHasMilliseconds() {
        assertEquals("2026-09-30T15:00:00.000Z", Instant.parse("2026-09-30T15:00:00Z").toDatetimeString())
        assertEquals("2026-09-30T15:00:00.120Z", Instant.parse("2026-09-30T15:00:00.12Z").toDatetimeString())
    }

    @Test
    fun toDatetimeString_dropsSubMilliseconds() {
        assertEquals("2026-09-30T15:00:00.123Z", Instant.parse("2026-09-30T15:00:00.123456789Z").toDatetimeString())
    }

    @Test
    fun toDatetimeString_sortsAsStringInTimeOrder() {
        val instants = listOf(
            "2026-09-30T14:59:59.999Z",
            "2026-09-30T15:00:00Z",
            "2026-09-30T15:00:00.001Z",
            "2026-09-30T15:00:01Z",
        ).map(Instant::parse)

        val strings = instants.map { it.toDatetimeString() }

        assertEquals(strings.sorted(), strings)
    }

    @Test
    fun toDatetimeString_matchesCloudFunctionsFormatAtBoundary() {
        /* Cloud Functions write toISOString(), e.g. "2026-09-30T15:00:00.000Z". */
        val bound = Instant.parse("2026-09-30T15:00:00Z").toDatetimeString()

        assertTrue("2026-09-30T15:00:00.000Z" >= bound)
        assertTrue("2026-09-30T14:59:59.999Z" < bound)
    }

    @Test
    fun toDomain_readsBothOldAndNewFormats() {
        val old = ExpenseDto(id = "1", timestamp = 1L, datetime = "2026-09-30T15:00:00Z", amount = 1, generatedType = "manual")
        val new = old.copy(datetime = "2026-09-30T15:00:00.000Z")

        assertEquals(old.toDomain().datetime, new.toDomain().datetime)
    }
}
