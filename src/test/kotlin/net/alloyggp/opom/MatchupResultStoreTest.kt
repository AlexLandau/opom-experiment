package net.alloyggp.opom

import org.junit.Assert.assertEquals
import kotlin.test.Test

class MatchupResultStoreTest {
    @Test fun testIndices1() {
        val mrs = MatchupResultStore(listOf("foo", "bar", "baz"))
        assertEquals(0, mrs.getMatchupIndex(0, 1))
        assertEquals(1, mrs.getMatchupIndex(0, 2))
        assertEquals(2, mrs.getMatchupIndex(1, 2))
    }
    @Test fun testIndices2() {
        val mrs = MatchupResultStore(listOf("one", "two", "three", "four"))
        assertEquals(0, mrs.getMatchupIndex(0, 1))
        assertEquals(1, mrs.getMatchupIndex(0, 2))
        assertEquals(2, mrs.getMatchupIndex(0, 3))
        assertEquals(3, mrs.getMatchupIndex(1, 2))
        assertEquals(4, mrs.getMatchupIndex(1, 3))
        assertEquals(5, mrs.getMatchupIndex(2, 3))
    }
}
