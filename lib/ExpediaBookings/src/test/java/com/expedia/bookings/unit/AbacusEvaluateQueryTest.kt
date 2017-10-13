package com.expedia.bookings.unit

import com.expedia.bookings.data.abacus.AbacusEvaluateQuery
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AbacusEvaluateQueryTest {

    @Test
    fun testDuplicateTestIdsNotAllowed() {
        val query = AbacusEvaluateQuery("TEST-TEST-TEST-TEST", 1, 0)
        val tests = listOf(1,1,1,2)
        query.addExperiments(tests)

        val evaluatedExperiments = query.evaluatedExperiments

        assertEquals(2, evaluatedExperiments.size)
        assertTrue(evaluatedExperiments.contains(1) && evaluatedExperiments.contains(2))
    }
}