package com.expedia.bookings.data.abacus

import com.expedia.bookings.analytics.AppAnalytics
import org.junit.Test
import java.lang.reflect.Modifier
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AbacusUtilsTest {
    @Test
    fun testGetActiveTestsMatchesDeclaredTests() {
        val declaredTestKeys = AbacusUtils::class.java.declaredFields
                .filter {
                    ((it.modifiers and (Modifier.FINAL or Modifier.STATIC)) != 0) && ABTest::class.java == it.type
                }
                .map { (it.get(null) as? ABTest)?.key }
        val activeTestKeys = AbacusUtils.getActiveTests()

        assertEquals(declaredTestKeys.size, activeTestKeys.size, "Declared test keys and active test keys sizes do not match.")

        declaredTestKeys.forEach {
            assertTrue(activeTestKeys.contains(it), "Active test keys does not contain $it")
        }
    }

    @Test
    fun testGetAnalyticsStringWithNullAbacusTest() {
        assertEquals("", AbacusUtils.getAnalyticsString(null))
    }

    @Test
    fun testGetAnalyticsStringFormatWithPopulatedAbacusTest() {
        val mockTest = AbacusTest()
        mockTest.id = 1
        mockTest.instanceId = 2
        mockTest.value = 3

        assertEquals("1.2.3", AbacusUtils.getAnalyticsString(mockTest))
    }

    @Test
    fun testAppendStringWithNullKeyReturnsEmptyString() {
        assertEquals("", AbacusUtils.appendString(null))
    }

    @Test
    fun testAppendStringWithEmptyKeyReturnsEmptyString() {
        assertEquals("", AbacusUtils.appendString(""))
    }

    @Test
    fun testConstructedAnalyticsStringUsingAppendString() {
        val mockTest = AbacusTest()
        mockTest.id = 1
        mockTest.instanceId = 2
        mockTest.value = 3

        val analytics = AppAnalytics()
        analytics.setProp(34, "aKey")

        val analyticsString = AbacusUtils.appendString(analytics.getProp(34)) + AbacusUtils.getAnalyticsString(mockTest)

        assertEquals("aKey|1.2.3", analyticsString)
    }
}
